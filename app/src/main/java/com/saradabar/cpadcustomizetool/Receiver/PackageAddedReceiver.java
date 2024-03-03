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