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

package com.saradabar.cpadcustomizetool;

import static com.saradabar.cpadcustomizetool.util.Common.isDhizukuActive;
import static com.saradabar.cpadcustomizetool.util.Common.parseJson;

import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener;
import com.saradabar.cpadcustomizetool.data.connection.AsyncFileDownload;
import com.saradabar.cpadcustomizetool.data.connection.Updater;
import com.saradabar.cpadcustomizetool.data.event.DownloadEventListener;
import com.saradabar.cpadcustomizetool.data.handler.CrashHandler;
import com.saradabar.cpadcustomizetool.data.handler.ProgressHandler;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;
import com.saradabar.cpadcustomizetool.util.Variables;
import com.saradabar.cpadcustomizetool.view.activity.CrashLogActivity;
import com.saradabar.cpadcustomizetool.view.activity.StartActivity;
import com.saradabar.cpadcustomizetool.view.activity.WebViewActivity;
import com.saradabar.cpadcustomizetool.view.activity.WelAppActivity;
import com.saradabar.cpadcustomizetool.view.views.SingleListView;
import com.stephentuso.welcome.WelcomeHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class MainActivity extends AppCompatActivity implements DownloadEventListener {

    IDchaService mDchaService;

    @Deprecated
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        setContentView(R.layout.layout_progress);

        /* 前回クラッシュしているかどうか */
        if (Preferences.load(this, Constants.KEY_FLAG_CRASH_LOG, false)) {
            /* クラッシュダイアログ表示 */
            crashError();
        } else {
            /* 起動処理 */
            firstCheck();
        }
    }

    @Deprecated
    private void crashError() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setMessage(getString(R.string.dialog_error_crash, getApplicationInfo().loadLabel(getPackageManager())))
                .setPositiveButton(R.string.dialog_common_continue, (dialog, which) -> {
                    Preferences.save(this, Constants.KEY_FLAG_CRASH_LOG, false);
                    firstCheck();
                })
                .setNeutralButton(R.string.dialog_common_check, (dialog, which) -> {
                    startActivity(new Intent(this, CrashLogActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    finish();
                })
                .show();
    }

    @Deprecated
    private void firstCheck() {
        /* 初回起動か確認 */
        if (Preferences.load(this, Constants.KEY_FLAG_SETTINGS, false)) {
            /* 初回起動ではないならサポート端末か確認 */
            if (supportModelCheck()) {
                /* DchaServiceを確認 */
                checkDchaService();
            } else {
                supportModelError();
            }
        } else {
            /* 初回起動はアップデート確認後ウォークスルー起動 */
            /* アップデートチェック */
            updateCheck();
        }
    }

    /* アップデートチェック */
    @Deprecated
    private void updateCheck() {
        showLoadingDialog();
        new AsyncFileDownload(this, Constants.URL_CHECK, new File(new File(getExternalCacheDir(), "Check.json").getPath()), Constants.REQUEST_DOWNLOAD_UPDATE_CHECK).execute();
    }

    /* ダウンロード完了 */
    @Deprecated
    @Override
    public void onDownloadComplete(int reqCode) {
        switch (reqCode) {
            /* アップデートチェック要求の場合 */
            case Constants.REQUEST_DOWNLOAD_UPDATE_CHECK:
                try {
                    JSONObject jsonObj1 = parseJson(this);
                    JSONObject jsonObj2 = jsonObj1.getJSONObject("ct");
                    JSONObject jsonObj3 = jsonObj2.getJSONObject("update");
                    Variables.DOWNLOAD_FILE_URL = jsonObj3.getString("url");

                    if (jsonObj3.getInt("versionCode") > BuildConfig.VERSION_CODE) {
                        cancelLoadingDialog();
                        showUpdateDialog(jsonObj3.getString("description"));
                    } else {
                        cancelLoadingDialog();
                        new WelcomeHelper(this, WelAppActivity.class).forceShow();
                    }
                } catch (JSONException | IOException ignored) {
                }
                break;
            /* APKダウンロード要求の場合 */
            case Constants.REQUEST_DOWNLOAD_APK:
                new Handler().post(() -> new Updater(this).installApk(this, 0));
                break;
            default:
                break;
        }
    }

    /* ダウンロードエラー */
    @Override
    public void onDownloadError(int reqCode) {
        cancelLoadingDialog();
        new WelcomeHelper(this, WelAppActivity.class).forceShow();
    }


    /* サーバー接続エラー */
    @Override
    public void onConnectionError(int reqCode) {
        cancelLoadingDialog();
        new WelcomeHelper(this, WelAppActivity.class).forceShow();
    }

    /* アップデートダイアログ */
    @Deprecated
    private void showUpdateDialog(String str) {
        /* モデルIDをセット */
        switch (Build.MODEL) {
            case "TAB-A03-BR3":
                Preferences.save(this, Constants.KEY_MODEL_NAME, Constants.MODEL_CT3);
                break;
            case "TAB-A05-BD":
                Preferences.save(this, Constants.KEY_MODEL_NAME, Constants.MODEL_CTX);
                break;
            case "TAB-A05-BA1":
                Preferences.save(this, Constants.KEY_MODEL_NAME, Constants.MODEL_CTZ);
                break;
            default:
                Preferences.save(this, Constants.KEY_MODEL_NAME, Constants.MODEL_CT2);
                break;
        }

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
                .setTitle(R.string.dialog_title_update)
                .setMessage("アップデートモードを変更するには”設定”を押下してください")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    LinearProgressIndicator linearProgressIndicator = findViewById(R.id.layout_progress_main);
                    linearProgressIndicator.show();
                    AsyncFileDownload asyncFileDownload = new AsyncFileDownload(this, Variables.DOWNLOAD_FILE_URL, new File(new File(getExternalCacheDir(), "update.apk").getPath()), Constants.REQUEST_DOWNLOAD_APK);
                    asyncFileDownload.execute();
                    ProgressHandler progressHandler = new ProgressHandler();
                    progressHandler.linearProgressIndicator = linearProgressIndicator;
                    progressHandler.textView = findViewById(R.id.layout_text_progress);
                    progressHandler.asyncfiledownload = asyncFileDownload;
                    progressHandler.sendEmptyMessage(0);
                })
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> {
                    new WelcomeHelper(this, WelAppActivity.class).forceShow();
                })
                .setNeutralButton(R.string.dialog_common_settings, (dialog2, which2) -> {
                    View v = getLayoutInflater().inflate(R.layout.layout_update_list, null);
                    List<SingleListView.AppData> dataList = new ArrayList<>();
                    int i = 0;

                    for (String str1 : Constants.list) {
                        SingleListView.AppData data = new SingleListView.AppData();
                        data.label = str1;
                        data.updateMode = i;
                        dataList.add(data);
                        i++;
                    }

                    ListView listView = v.findViewById(R.id.update_list);
                    listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                    listView.setAdapter(new SingleListView.AppListAdapter(v.getContext(), dataList));
                    listView.setOnItemClickListener((parent, mView, position, id) -> {
                        switch (position) {
                            case 0:
                                if (Preferences.load(v.getContext(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CT2 || Preferences.load(v.getContext(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CT3) {
                                    Preferences.save(v.getContext(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                                    listView.invalidateViews();
                                } else {
                                    new MaterialAlertDialogBuilder(v.getContext())
                                            .setMessage(getString(R.string.dialog_error_not_work_mode))
                                            .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                            .show();
                                }
                                break;
                            case 1:
                                Preferences.save(v.getContext(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                                listView.invalidateViews();
                                break;
                            case 2:
                                if (tryBindDchaService() && Preferences.load(v.getContext(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) != Constants.MODEL_CT2) {
                                    Preferences.save(v.getContext(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                                    listView.invalidateViews();
                                } else {
                                    new MaterialAlertDialogBuilder(v.getContext())
                                            .setMessage(getString(R.string.dialog_error_not_work_mode))
                                            .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                            .show();
                                }
                                break;
                            case 3:
                                if (((DevicePolicyManager) v.getContext().getSystemService(Context.DEVICE_POLICY_SERVICE)).isDeviceOwnerApp(v.getContext().getPackageName()) && Preferences.load(v.getContext(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) != Constants.MODEL_CT2) {
                                    Preferences.save(v.getContext(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                                    listView.invalidateViews();
                                } else {
                                    new MaterialAlertDialogBuilder(v.getContext())
                                            .setMessage(getString(R.string.dialog_error_not_work_mode))
                                            .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                            .show();
                                }
                                break;
                            case 4:
                                if (isDhizukuActive(v.getContext())) {
                                    Preferences.save(v.getContext(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                                    listView.invalidateViews();
                                } else {
                                    if (!Dhizuku.init(v.getContext())) {
                                        new MaterialAlertDialogBuilder(v.getContext())
                                                .setMessage(getString(R.string.dialog_error_not_work_mode))
                                                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                                .show();
                                    }

                                    Dhizuku.requestPermission(new DhizukuRequestPermissionListener() {
                                        @Override
                                        public void onRequestPermission(int grantResult) {
                                            runOnUiThread(() -> {
                                                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                                                    Preferences.save(v.getContext(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                                                    listView.invalidateViews();
                                                } else {
                                                    new MaterialAlertDialogBuilder(v.getContext())
                                                            .setMessage(R.string.dialog_info_dhizuku_deny_permission)
                                                            .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                                            .show();
                                                }
                                            });
                                        }
                                    });
                                }
                                break;
                            case 5:
                                new MaterialAlertDialogBuilder(v.getContext())
                                        .setMessage(getString(R.string.dialog_error_not_work_mode))
                                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                        .show();
                        }
                    });

                    new MaterialAlertDialogBuilder(v.getContext())
                            .setView(v)
                            .setTitle(getString(R.string.dialog_title_select_mode))
                            .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> firstCheck())
                            .show();
                })
                .show();
    }

    /* ローディングダイアログを表示する */
    private void showLoadingDialog() {
        TextView textView = findViewById(R.id.layout_text_progress);
        textView.setText("サーバーと通信しています...");
        LinearProgressIndicator linearProgressIndicator = findViewById(R.id.layout_progress_main);
        linearProgressIndicator.show();
    }

    /* ローディングダイアログを非表示にする */
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

    /* 端末チェック */
    private boolean supportModelCheck() {
        String[] modelName = {"TAB-A03-BS", "TAB-A03-BR", "TAB-A03-BR2", "TAB-A03-BR3", "TAB-A05-BD", "TAB-A05-BA1"};

        for (String string : modelName) {
            if (Objects.equals(string, Build.MODEL)) {
                return true;
            }
        }
        return false;
    }

    /* 端末チェックエラー */
    private void supportModelError() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_common_error)
                .setMessage(R.string.dialog_error_start_cpad)
                .setIcon(R.drawable.alert)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                .show();
    }

    /* DchaService動作チェック */
    @Deprecated
    private void checkDchaService() {
        /* DchaServiceを使用するか確認 */
        if (Preferences.load(this, Constants.KEY_FLAG_DCHA_SERVICE, false)) {
            if (!tryBindDchaService()) {
                new MaterialAlertDialogBuilder(this)
                        .setCancelable(false)
                        .setTitle(R.string.dialog_title_common_error)
                        .setMessage(R.string.dialog_error_start_dcha_service)
                        .setIcon(R.drawable.alert)
                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> finishAndRemoveTask())
                        .setNeutralButton(R.string.dialog_common_continue, (dialogInterface, i) -> {
                            Preferences.save(this, Constants.KEY_FLAG_DCHA_SERVICE, false);
                            switch (Build.MODEL) {
                                case "TAB-A03-BR3":
                                    confCheckCT3();
                                    break;
                                case "TAB-A05-BD":
                                    confCheckCTX();
                                    break;
                                case "TAB-A05-BA1":
                                    confCheckCTZ();
                                    break;
                                default:
                                    confCheckCT2();
                                    break;
                            }
                        })
                        .show();
                return;
            }
        }

        switch (Build.MODEL) {
            case "TAB-A03-BR3":
                confCheckCT3();
                break;
            case "TAB-A05-BD":
                confCheckCTX();
                break;
            case "TAB-A05-BA1":
                confCheckCTZ();
                break;
            default:
                confCheckCT2();
                break;
        }
    }

    /* Pad2起動設定チェック */
    @Deprecated
    private void confCheckCT2() {
        Preferences.save(this, Constants.KEY_MODEL_NAME, Constants.MODEL_CT2);

        if (Preferences.load(this, Constants.KEY_FLAG_SETTINGS, false)) {
            startActivity(new Intent(this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            overridePendingTransition(0, 0);
            finish();
        } else {
            WarningDialog();
        }
    }

    /* Pad3起動設定チェック */
    @Deprecated
    private void confCheckCT3() {
        Preferences.save(this, Constants.KEY_MODEL_NAME, Constants.MODEL_CT3);

        if (Preferences.load(this, Constants.KEY_FLAG_SETTINGS, false)) {
            if (isPermissionCheck()) {
                startActivity(new Intent(this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                overridePendingTransition(0, 0);
                finish();
            }
        } else {
            WarningDialog();
        }
    }

    /* NEO起動設定チェック */
    @Deprecated
    private void confCheckCTX() {
        Preferences.save(this, Constants.KEY_MODEL_NAME, Constants.MODEL_CTX);

        if (Preferences.load(this, Constants.KEY_FLAG_SETTINGS, false)) {
            if (isPermissionCheck()) {
                startActivity(new Intent(this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                overridePendingTransition(0, 0);
                finish();
            }
        } else {
            WarningDialog();
        }
    }

    /* NEXT起動設定チェック */
    @Deprecated
    private void confCheckCTZ() {
        Preferences.save(this, Constants.KEY_MODEL_NAME, Constants.MODEL_CTZ);

        if (Preferences.load(this, Constants.KEY_FLAG_SETTINGS, false)) {
            if (isPermissionCheck()) {
                startActivity(new Intent(this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                overridePendingTransition(0, 0);
                finish();
            }
        } else {
            WarningDialog();
        }
    }

    /* 初回起動お知らせ */
    @Deprecated
    public void WarningDialog() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(false)
                .setTitle(R.string.dialog_title_notice_start)
                .setMessage(R.string.dialog_notice_start)
                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> {
                    if (isPermissionCheck()) {
                        Preferences.save(this, Constants.KEY_FLAG_SETTINGS, true);
                        startActivity(new Intent(this, StartActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        overridePendingTransition(0, 0);
                        finish();
                    }
                })
                .show();
    }

    /* システム設定変更権限か付与されているか確認 */
    @Deprecated
    private boolean isPermissionCheck() {
        if (isWriteSystemPermissionCheck()) {
            new MaterialAlertDialogBuilder(this)
                    .setCancelable(false)
                    .setTitle(R.string.dialog_title_grant_permission)
                    .setMessage(R.string.dialog_error_start_permission)
                    .setIcon(R.drawable.alert)
                    .setPositiveButton(R.string.dialog_common_settings, (dialog, which) -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            startActivityForResult(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", getPackageName(), null)).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), Constants.REQUEST_ACTIVITY_PERMISSION);
                        }
                    })
                    .setNeutralButton(R.string.dialog_common_exit, (dialogInterface, i) -> finishAndRemoveTask())
                    .show();
            return false;
        } else {
            return true;
        }
    }

    /* システム設定変更権限チェック */
    private boolean isWriteSystemPermissionCheck() {
        boolean canWrite = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canWrite = Settings.System.canWrite(this);
        }
        return !canWrite;
    }

    ServiceConnection mDchaServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDchaService = IDchaService.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /* DchaServiceへバインド */
    public boolean tryBindDchaService() {
        return bindService(Constants.DCHA_SERVICE, mDchaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Deprecated
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_ACTIVITY_UPDATE:
                if (Preferences.load(this, Constants.KEY_FLAG_SETTINGS, false)) {
                    if (supportModelCheck()) {
                        checkDchaService();
                    } else {
                        supportModelError();
                    }
                } else {
                    new WelcomeHelper(this, WelAppActivity.class).forceShow();
                }
                break;
            case WelcomeHelper.DEFAULT_WELCOME_SCREEN_REQUEST:
            case Constants.REQUEST_ACTIVITY_PERMISSION:
                if (supportModelCheck()) {
                    checkDchaService();
                } else {
                    supportModelError();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDchaService != null) {
            unbindService(mDchaServiceConnection);
        }
    }
}
