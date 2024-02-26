package com.saradabar.cpadcustomizetool.Receiver;

import static com.saradabar.cpadcustomizetool.util.Common.isDhizukuActive;
import static com.saradabar.cpadcustomizetool.util.Common.tryBindDhizukuService;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;

import com.saradabar.cpadcustomizetool.data.handler.CrashHandler;
import com.saradabar.cpadcustomizetool.data.service.KeepService;
import com.saradabar.cpadcustomizetool.util.Common;
import com.saradabar.cpadcustomizetool.view.activity.StartActivity;
import com.saradabar.cpadcustomizetool.view.flagment.DeviceOwnerFragment;

import java.util.Objects;

public class PackageAddedReceiver extends BroadcastReceiver {

    @Deprecated
    @Override
    public void onReceive(Context context, Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(context));
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(context);

        if (Objects.equals(intent.getAction(), Intent.ACTION_PACKAGE_ADDED)) {
            if (Objects.requireNonNull(intent.getData()).toString().replace("package:", "").equals(context.getPackageName())) {
                context.startService(new Intent(context, KeepService.class));
            }

            if (sp.getBoolean("permission_forced", false)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isDhizukuActive(context)) {
                        if (tryBindDhizukuService(context)) {
                            Runnable runnable = () -> {
                                for (ApplicationInfo app : context.getPackageManager().getInstalledApplications(0)) {
                                    /* ユーザーアプリか確認 */
                                    if (app.sourceDir.startsWith("/data/app/")) {
                                        Common.setPermissionGrantState(context, app.packageName, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                                    }
                                }
                            };
                            new Handler().postDelayed(runnable, 5000);
                        }
                    } else {
                        for (ApplicationInfo app : context.getPackageManager().getInstalledApplications(0)) {
                            /* ユーザーアプリか確認 */
                            if (app.sourceDir.startsWith("/data/app/")) {
                                Common.setPermissionGrantState(context, app.packageName, DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                            }
                        }
                    }
                }
            }
        }

        if (Objects.equals(intent.getAction(), Intent.ACTION_PACKAGE_REPLACED)) {
            if (Objects.requireNonNull(intent.getData()).toString().replace("package:", "").equals(context.getPackageName())) {
                context.startService(new Intent(context, KeepService.class));
            }
        }
    }
}