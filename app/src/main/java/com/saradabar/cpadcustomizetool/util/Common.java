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

package com.saradabar.cpadcustomizetool.util;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuUserServiceArgs;
import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;
import com.saradabar.cpadcustomizetool.data.service.DhizukuService;
import com.saradabar.cpadcustomizetool.data.service.IDhizukuService;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Common {

    public static IDhizukuService mDhizukuService;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setPermissionGrantState(Context context, String packageName, int grantState) {
        if (isDhizukuActive(context)) {
            if (tryBindDhizukuService(context)) {
                try {
                    for (String permission : getRuntimePermissions(context, packageName)) {
                        mDhizukuService.setPermissionGrantState(packageName, permission, grantState);
                    }
                } catch (RemoteException ignored) {
                }
            }
        } else {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            for (String permission : getRuntimePermissions(context, packageName)) {
                dpm.setPermissionGrantState(new ComponentName(context, AdministratorReceiver.class), packageName, permission, grantState);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String[] getRuntimePermissions(Context context, String packageName) {
        return new ArrayList<>(Arrays.asList(getRequiredPermissions(context, packageName))).toArray(new String[0]);
    }

    public static String[] getRequiredPermissions(Context context, String packageName) {
        try {
            String[] str = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
            if (str != null && str.length > 0) {
                return str;
            } else {
                return new String[0];
            }
        } catch (Exception ignored) {
            return new String[0];
        }
    }

    public static String getNowDate() {
        DateFormat df = new SimpleDateFormat("MMM dd HH:mm:ss.SSS z yyyy", Locale.ENGLISH);
        return df.format(System.currentTimeMillis());
    }

    @Deprecated
    public static void LogOverWrite(Context context, @NonNull Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        String message = "- ログ開始 -\n" +
                getNowDate() + ":\n" +
                "- デバイス情報:\n" +
                Build.FINGERPRINT + "\n" +
                "- 例外原因:\n" +
                throwable.getCause() + "\n" +
                "- スタックトレース\n" +
                stringWriter +
                "- ログ終了 -\n\n";

        if (!Preferences.load(context, Constants.KEY_CRASH_LOG, "").equals("")) {
            Preferences.save(context, Constants.KEY_CRASH_LOG, String.join(",", Preferences.load(context, Constants.KEY_CRASH_LOG, "")).replace("    ", "") + message);
        } else {
            Preferences.save(context, Constants.KEY_CRASH_LOG, message);
        }
    }

    /* 選択したファイルデータを取得 */
    @Nullable
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

    public static boolean isDhizukuActive(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm.isDeviceOwnerApp("com.rosan.dhizuku")) {
            if (Dhizuku.init(context)) {
                return Dhizuku.isPermissionGranted();
            }
        }
        return false;
    }

    public static boolean tryBindDhizukuService(Context context) {
        DhizukuUserServiceArgs args = new DhizukuUserServiceArgs(new ComponentName(context, DhizukuService.class));
        return Dhizuku.bindUserService(args, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                mDhizukuService = IDhizukuService.Stub.asInterface(iBinder);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        });
    }

    @Deprecated
    public static boolean isCfmDialog(Context context) {
        if (Preferences.load(context, Constants.KEY_MODEL_NAME, 0) == Constants.MODEL_CTX || Preferences.load(context, Constants.KEY_MODEL_NAME, 0) == Constants.MODEL_CTZ) {
            /* チャレパNEO・NEXTは対象 */
            if (!Constants.COUNT_DCHA_COMPLETED_FILE.exists() && Constants.IGNORE_DCHA_COMPLETED_FILE.exists() || !Constants.COUNT_DCHA_COMPLETED_FILE.exists() || Constants.IGNORE_DCHA_COMPLETED_FILE.exists()) {
                return Preferences.load(context, Constants.KEY_FLAG_CONFIRMATION, false);
            } else {
                return true;
            }
        } else {
            /* チャレパ２・３は対象外 */
            return true;
        }
    }

    public static long getFileSize(final File file) {
        if (file == null || !file.exists())
            return 0;
        if (!file.isDirectory())
            return file.length();
        final List<File> dirs = new LinkedList<>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                // Note: if you want to get physical size and not just logical size, include directories too to the result, and not just normal files
                if (child.isDirectory()) {
                    dirs.add(child);
                } else {
                    result += child.length();
                }
            }
        }
        return result;
    }
}