package com.saradabar.cpadcustomizetool.data.service;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import androidx.annotation.Keep;
import androidx.annotation.RequiresApi;

import com.rosan.dhizuku.shared.DhizukuVariables;
import com.saradabar.cpadcustomizetool.data.installer.SplitInstaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
    public boolean tryInstallPackages(String packageName, String[] installData) throws RemoteException {

        int sessionId;

        try {
            sessionId = createSession(context, context.getPackageManager().getPackageInstaller());
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
            if (commitSession(context.getPackageManager().getPackageInstaller(), sessionId, context)) {
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

    private int createSession(Context context, PackageInstaller packageInstaller) throws IOException {

        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pre_owner_install_location", false)) {
            params.setInstallLocation(PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL);
        } else params.setInstallLocation(PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY);
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

    private boolean commitSession(PackageInstaller packageInstaller, int sessionId, Context context) throws IOException {

        PackageInstaller.Session session = null;

        try {
            session = packageInstaller.openSession(sessionId);
            Intent intent = new Intent(context, DeviceOwnerService.class).putExtra("REQUEST_SESSION", sessionId);
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