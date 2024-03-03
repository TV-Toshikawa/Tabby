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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.saradabar.cpadcustomizetool.data.handler.CrashHandler;
import com.saradabar.cpadcustomizetool.util.Constants;

public class ProtectKeepService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);

        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
            tryBind();
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    private void tryBind() {
        bindService(Constants.KEEP_SERVICE, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);

            if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
                startService(Constants.KEEP_SERVICE);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);

        if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
            startService(Constants.PROTECT_KEEP_SERVICE);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}