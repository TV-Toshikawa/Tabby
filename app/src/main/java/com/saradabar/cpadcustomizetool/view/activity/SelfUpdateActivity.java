/* CPad Customize Tool
 * Copyright © 2021-2024 Kobold831 <146227823+kobold831@users.noreply.github.com>
 *
 * CPad Customize Tool（以下本ソフトウェアという）はオープンソフトウェアです。
 * これは、Apacheソフトウェア財団 によって発行された Apache License 2.0 （以下本ライセンスという）の条件に基づいています。
 * 本ソフトウェアの著作権法に定義される利用は本ライセンスに定義された範囲でいかなる行為をすることができます。
 *
 * Kobold831（以下著作権者という）は著作権法に定義されるこのプロジェクト全体の著作物（以下著作物という）の、
 * 著作権法に定義される著作権（以下著作権という）かつ著作権法に定義される著作人格権を有しておりまた放棄していません。
 * 本ソフトウェアを本ライセンスの範囲を超えて使用、複製、配布された場合、
 * 侵害行為地の著作権法が適用され著作権者は著作権法で定義される差止請求権を行使して著作権法に定義される差止請求を行います。
 *
 */

package com.saradabar.cpadcustomizetool.view.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.saradabar.cpadcustomizetool.BuildConfig;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.data.connection.AsyncFileDownload;
import com.saradabar.cpadcustomizetool.data.connection.Updater;
import com.saradabar.cpadcustomizetool.data.event.DownloadEventListener;
import com.saradabar.cpadcustomizetool.data.handler.ProgressHandler;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Toast;
import com.saradabar.cpadcustomizetool.util.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SelfUpdateActivity extends AppCompatActivity implements DownloadEventListener {

    @Deprecated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_progress);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        showLoadingDialog();
        new AsyncFileDownload(this, Constants.URL_CHECK, new File(new File(getExternalCacheDir(), "Check.json").getPath()), Constants.REQUEST_DOWNLOAD_UPDATE_CHECK).execute();
    }

    public JSONObject parseJson() throws JSONException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(getExternalCacheDir(), "Check.json").getPath()));
        JSONObject json;

        StringBuilder data = new StringBuilder();
        String str = bufferedReader.readLine();

        while(str != null){
            data.append(str);
            str = bufferedReader.readLine();
        }

        json = new JSONObject(data.toString());

        bufferedReader.close();

        return json;
    }

    @Deprecated
    @Override
    public void onDownloadComplete(int reqCode) {
        switch (reqCode) {
            case Constants.REQUEST_DOWNLOAD_UPDATE_CHECK:
                try {
                    JSONObject jsonObj1 = parseJson();
                    JSONObject jsonObj2 = jsonObj1.getJSONObject("ct");
                    JSONObject jsonObj3 = jsonObj2.getJSONObject("update");
                    Variables.DOWNLOAD_FILE_URL = jsonObj3.getString("url");

                    if (jsonObj3.getInt("versionCode") > BuildConfig.VERSION_CODE) {
                        cancelLoadingDialog();
                        showUpdateDialog(jsonObj3.getString("description"));
                    } else {
                        cancelLoadingDialog();
                        showNoUpdateDialog();
                    }
                } catch (JSONException | IOException ignored) {
                }
                break;
            case Constants.REQUEST_DOWNLOAD_APK:
                new Handler().post(() -> new Updater(this).installApk(this, 0));
                break;
            default:
                break;
        }
    }

    @Override
    public void onDownloadError(int reqCode) {
        cancelLoadingDialog();
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_error)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> finish())
                .show();
    }

    @Override
    public void onConnectionError(int reqCode) {
        cancelLoadingDialog();
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setIcon(R.drawable.alert)
                .setMessage(R.string.dialog_error_start_connection)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> finish())
                .show();
    }

    @Deprecated
    private void showUpdateDialog(String str) {
        View view = getLayoutInflater().inflate(R.layout.view_update, null);
        TextView tv = view.findViewById(R.id.update_information);

        tv.setText(str);

        view.findViewById(R.id.update_info_button).setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, WebViewActivity.class).putExtra("URL", Constants.URL_UPDATE_INFO).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
                Toast.toast(this, R.string.toast_unknown_activity);
            }
        });

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    AsyncFileDownload asyncFileDownload = new AsyncFileDownload(this, Variables.DOWNLOAD_FILE_URL, new File(new File(getExternalCacheDir(), "update.apk").getPath()), Constants.REQUEST_DOWNLOAD_APK);
                    asyncFileDownload.execute();
                    ProgressHandler progressHandler = new ProgressHandler();
                    progressHandler.linearProgressIndicator = findViewById(R.id.layout_progress_main);
                    progressHandler.textView = findViewById(R.id.layout_text_progress);
                    progressHandler.asyncfiledownload = asyncFileDownload;
                    progressHandler.sendEmptyMessage(0);
                })
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> finish())
                .show();
    }

    private void showNoUpdateDialog() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_update)
                .setMessage(R.string.dialog_info_no_update)
                .setPositiveButton(R.string.dialog_common_ok,
                        (dialog, which) -> finish())
                .show();
    }

    private void showLoadingDialog() {
        TextView textView = findViewById(R.id.layout_text_progress);
        textView.setText("サーバーと通信しています...");
        LinearProgressIndicator linearProgressIndicator = findViewById(R.id.layout_progress_main);
        linearProgressIndicator.show();
    }

    private void cancelLoadingDialog() {
        TextView textView = findViewById(R.id.layout_text_progress);
        textView.setText("");
        try {
            LinearProgressIndicator linearProgressIndicator = findViewById(R.id.layout_progress_main);
            if (linearProgressIndicator.isShown()) {
                linearProgressIndicator.hide();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_ACTIVITY_UPDATE) {
            finish();
        }
    }
}