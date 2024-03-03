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

package com.saradabar.cpadcustomizetool.data.service;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.RemoteException;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;

import com.rosan.dhizuku.shared.DhizukuVariables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DhizukuService extends IDhizukuService.Stub {

    private Context context;
    private DevicePolicyManager dpm;

    @Keep
    public DhizukuService(Context context) {
        this.context = context;
        dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public void setUninstallBlocked(String packageName, boolean uninstallBlocked) throws RemoteException {
        dpm.setUninstallBlocked(DhizukuVariables.COMPONENT_NAME, packageName, uninstallBlocked);
    }

    @Override
    public boolean isUninstallBlocked(String packageName) throws RemoteException {
        return dpm.isUninstallBlocked(DhizukuVariables.COMPONENT_NAME, packageName);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setPermissionPolicy(int policy) {
        dpm.setPermissionPolicy(DhizukuVariables.COMPONENT_NAME, policy);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setPermissionGrantState(String packageName, String permission, int grantState) {
        dpm.setPermissionGrantState(DhizukuVariables.COMPONENT_NAME, packageName, permission, grantState);
    }

    @Override
    public boolean tryInstallPackages(String[] installData, int reqCode) throws RemoteException {

        int sessionId;

        try {
            sessionId = createSession(context.getPackageManager().getPackageInstaller());
            if (sessionId < 0) {
                context.getPackageManager().getPackageInstaller().abandonSession(sessionId);
                return false;
            }
        } catch (IOException ignored) {
            return false;
        }

        /* インストールデータの長さ回数繰り返す */
        for (String str : installData) {
            /* 配列の中身を確認 */
            if (str != null) {
                try {
                    if (!writeSession(context.getPackageManager().getPackageInstaller(), sessionId, new File(str))) {
                        context.getPackageManager().getPackageInstaller().abandonSession(sessionId);
                        return false;
                    }
                } catch (Exception e) {
                    context.getPackageManager().getPackageInstaller().abandonSession(sessionId);
                    return false;
                }
            } else {
                /* つぎの配列がnullなら終了 */
                break;
            }
        }

        try {
            if (commitSession(context.getPackageManager().getPackageInstaller(), sessionId, context, reqCode)) {
                return true;
            } else {
                context.getPackageManager().getPackageInstaller().abandonSession(sessionId);
                return false;
            }
        } catch (IOException ignored) {
            context.getPackageManager().getPackageInstaller().abandonSession(sessionId);
            return false;
        }
    }

    @Deprecated
    @Override
    public void clearDeviceOwnerApp(String packageName) throws RemoteException {
        dpm.clearDeviceOwnerApp(packageName);
    }

    private int createSession(PackageInstaller packageInstaller) throws IOException {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setInstallLocation(PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL);
        return packageInstaller.createSession(params);
    }

    private boolean writeSession(PackageInstaller packageInstaller, int sessionId, File apkFile) throws IOException {

        long sizeBytes = -1;
        String apkPath = apkFile.getAbsolutePath();
        File file = new File(apkPath);

        if (file.isFile()) {
            sizeBytes = file.length();
        }

        PackageInstaller.Session session = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            session = packageInstaller.openSession(sessionId);
            in = new FileInputStream(apkPath);
            out = session.openWrite(getRandomString(), 0, sizeBytes);
            byte[] buffer = new byte[65536];
            int c;

            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }

            session.fsync(out);
            return true;
        } catch (Exception ignored) {
            if (session != null) session.abandon();
            return false;
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
        }
    }

    private boolean commitSession(PackageInstaller packageInstaller, int sessionId, Context context, int reqCode) throws IOException {

        PackageInstaller.Session session = null;

        try {
            session = packageInstaller.openSession(sessionId);
            Intent intent = new Intent("com.saradabar.cpadcustomizetool.data.service.InstallService").setPackage("com.saradabar.cpadcustomizetool").putExtra("REQUEST_CODE", reqCode).putExtra("REQUEST_SESSION", sessionId);
            PendingIntent pendingIntent = PendingIntent.getService(
                    context,
                    sessionId,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );

            session.commit(pendingIntent.getIntentSender());
            return true;
        } catch (Exception ignored) {
            if (session != null) session.abandon();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    private String getRandomString() {

        String theAlphaNumericS;
        StringBuilder builder;
        theAlphaNumericS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        builder = new StringBuilder(5);

        for (int m = 0; m < 5; m++) {
            int myindex = (int) (theAlphaNumericS.length() * Math.random());
            builder.append(theAlphaNumericS.charAt(myindex));
        }
        return builder.toString();
    }
}