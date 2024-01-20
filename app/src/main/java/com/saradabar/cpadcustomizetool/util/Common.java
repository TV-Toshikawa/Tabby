package com.saradabar.cpadcustomizetool.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import androidx.annotation.RequiresApi;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class Common {

    public static ComponentName getAdministratorComponent(Context context) {
        return new ComponentName(context, com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver.class);
    }

    public static void setPermissionGrantState(Context context, String packageName, int i) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : getRuntimePermissions(context, packageName)) {
                devicePolicyManager.setPermissionGrantState(new ComponentName(context, AdministratorReceiver.class), packageName, permission, i);
            }
        }
    }

    public static String[] getRequiredPermissions(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            String[] str = packageInfo.requestedPermissions;
            if (str != null && str.length > 0) {
                return str;
            } else {
                return new String[0];
            }
        } catch (Exception ignored) {
            return new String[0];
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String[] getRuntimePermissions(Context context, String packageName) {
        return new ArrayList<>(Arrays.asList(getRequiredPermissions(context, packageName))).toArray(new String[0]);
    }

    public static String getNowDate() {
        DateFormat df = new SimpleDateFormat("MMM dd HH:mm:ss.SSS z yyyy", Locale.ENGLISH);
        return df.format(System.currentTimeMillis());
    }

    public static void LogOverWrite(Context context, Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        String message = "- ログ開始:\n" +
                getNowDate() + ":\n" +
                "- デバイス情報:\n" +
                Build.FINGERPRINT + "\n" +
                "- 例外原因:\n" +
                throwable.getCause() + "\n" +
                "- スタックトレース\n" +
                stringWriter + "\n" +
                "- ログ終了:\n";

        if (Preferences.GET_CRASH_LOG(context) != null) {
            Preferences.SAVE_CRASH_LOG(context, String.join(",", Preferences.GET_CRASH_LOG(context)).replace("    ", "") + message);
        } else {
            Preferences.SAVE_CRASH_LOG(context, message);
        }
    }

    /* 選択したファイルデータを取得 */
    public static String getFilePath(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            switch (Objects.requireNonNull(uri.getAuthority())) {
                /* 内部ストレージ */
                case "com.android.externalstorage.documents":
                    String[] str = DocumentsContract.getDocumentId(uri).split(":");
                    return Environment.getExternalStorageDirectory() + "/" + str[1];
                /* ダウンロード */
                case "com.android.providers.downloads.documents":
                    try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            return Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                        }
                    }
                default:
                    return null;
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /* 確認ダイアログ */
    public static boolean isCfmDialog(Context context) {
        if (!Constants.COUNT_DCHA_COMPLETED_FILE.exists() && Constants.IGNORE_DCHA_COMPLETED_FILE.exists() || !Constants.COUNT_DCHA_COMPLETED_FILE.exists() || Constants.IGNORE_DCHA_COMPLETED_FILE.exists()) {
            if (!Preferences.GET_CONFIRMATION(context)) {
                new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setTitle(context.getString(R.string.dialog_question_are_you_sure))
                        .setMessage(context.getString(R.string.dialog_confirmation))
                        .setPositiveButton(R.string.dialog_common_continue, (dialog, which) -> new AlertDialog.Builder(context)
                                .setCancelable(false)
                                .setTitle(context.getString(R.string.dialog_title_final_confirmation))
                                .setMessage(context.getString(R.string.dialog_final_confirmation))
                                .setPositiveButton(R.string.dialog_common_continue, (dialog1, which1) -> {
                                    Preferences.SET_CONFIRMATION(true, context);
                                    dialog1.dismiss();
                                })
                                .setNegativeButton(R.string.dialog_common_cancel, (dialog1, which1) -> dialog.dismiss())
                                .show())
                        .setNegativeButton(R.string.dialog_common_cancel, (dialog, which) -> dialog.dismiss())
                        .show();
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static JSONObject parseJson(Context context) throws JSONException, IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(context.getExternalCacheDir(), "Check.json").getPath()));
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
}