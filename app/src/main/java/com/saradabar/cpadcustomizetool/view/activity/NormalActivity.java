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

import static com.saradabar.cpadcustomizetool.util.Common.isCfmDialog;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.data.handler.CrashHandler;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;

import java.util.Objects;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class NormalActivity extends AppCompatActivity {

    IDchaService mDchaService;

    @Deprecated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        bindService(Constants.DCHA_SERVICE, mDchaServiceConnection, Context.BIND_AUTO_CREATE);

        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        Runnable runnable = () -> {
            if (!startCheck()) {
                Toast.toast(this, R.string.toast_not_completed_settings);
                finishAndRemoveTask();
                return;
            }

            if (!setSystemSettings()) {
                Toast.toast(this, R.string.toast_not_install_launcher);
                finishAndRemoveTask();
                return;
            }

            if (setDchaSettings()) {
                switch (Objects.requireNonNull(PreferenceManager.getDefaultSharedPreferences(this).getString("emergency_mode", ""))) {
                    case "1":
                        activityManager.killBackgroundProcesses("jp.co.benesse.touch.allgrade.b003.touchhomelauncher");
                    case "2":
                        activityManager.killBackgroundProcesses("jp.co.benesse.touch.home");
                }
                Toast.toast(this, R.string.toast_execution);
            }
            finishAndRemoveTask();
        };

        new Handler().postDelayed(runnable, 10);
    }

    @Deprecated
    private boolean startCheck() {
        return Preferences.load(this, Constants.KEY_FLAG_SETTINGS, false);
    }

    @Deprecated
    private boolean setSystemSettings() {
        ContentResolver resolver = getContentResolver();

        if (Preferences.isNormalModeSettingsDchaState(this)) {
            if (isCfmDialog(this)) {
                Settings.System.putInt(resolver, Constants.DCHA_STATE, 0);
            }
        }

        if (Preferences.isNormalModeSettingsNavigationBar(this))
            Settings.System.putInt(resolver, Constants.HIDE_NAVIGATION_BAR, 0);

        if (Objects.equals(Preferences.load(this, Constants.KEY_NORMAL_LAUNCHER, ""), ""))
            return false;

        if (Preferences.isNormalModeSettingsActivity(this)) {
            try {
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(Preferences.load(this, Constants.KEY_NORMAL_LAUNCHER, ""));
                startActivity(intent);
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    @Deprecated
    private boolean setDchaSettings() {
        if (!Preferences.load(getApplicationContext(), Constants.KEY_FLAG_DCHA_SERVICE, false)) {
            Toast.toast(getApplicationContext(), R.string.toast_use_not_dcha);
            return false;
        }

        ActivityInfo activityInfo = null;
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);

        if (resolveInfo != null) activityInfo = resolveInfo.activityInfo;

        if (Preferences.isNormalModeSettingsLauncher(getApplicationContext())) {
            try {
                if (activityInfo != null) {
                    mDchaService.clearDefaultPreferredApp(activityInfo.packageName);
                    mDchaService.setDefaultPreferredHomeApp(Preferences.load(getApplicationContext(), Constants.KEY_NORMAL_LAUNCHER, ""));
                }
            } catch (RemoteException ignored) {
                Toast.toast(getApplicationContext(), R.string.toast_not_install_launcher);
                finishAndRemoveTask();
            }
        }

        return true;
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

    @Override
    public void onPause() {
        super.onPause();
        finishAndRemoveTask();
    }
}