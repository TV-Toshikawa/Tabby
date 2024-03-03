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

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.saradabar.cpadcustomizetool.data.handler.CrashHandler;
import com.saradabar.cpadcustomizetool.data.service.KeepService;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Deprecated
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(context));
            ContentResolver resolver = context.getContentResolver();
            SharedPreferences sp = context.getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);

            /* UsbDebugを有効にするか確認 */
            try {
                if (sp.getBoolean(Constants.KEY_ENABLED_AUTO_USB_DEBUG, false)) {
                    String dchaStateString = Constants.DCHA_STATE;

                    try {
                        if (Preferences.load(context, Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTX || Preferences.load(context, Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTZ) {
                            Settings.System.putInt(resolver, dchaStateString, 3);
                        }

                        Thread.sleep(100);
                        Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, 1);

                        if (Preferences.load(context, Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTX || Preferences.load(context, Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTZ) {
                            Settings.System.putInt(resolver, dchaStateString, 0);
                        }
                    } catch (SecurityException | InterruptedException ignored) {
                        if (Preferences.load(context, Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTX || Preferences.load(context, Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTZ) {
                            Settings.System.putInt(resolver, dchaStateString, 0);
                        }

                        /* 権限が付与されていないなら機能を無効 */
                        SharedPreferences.Editor spe = sp.edit();
                        spe.putBoolean(Constants.KEY_ENABLED_AUTO_USB_DEBUG, false);
                        spe.apply();
                    }
                }
            }catch (NullPointerException ignored) {
                SharedPreferences.Editor spe = sp.edit();
                spe.putBoolean(Constants.KEY_ENABLED_AUTO_USB_DEBUG, false);
                spe.apply();
            }

            /* Serviceを起動するとナビゲーションバーが非表示になってしまうのを防ぐ */
            if (sp.getBoolean(Constants.KEY_ENABLED_KEEP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_DCHA_STATE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_MARKET_APP_SERVICE, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_USB_DEBUG, false) || sp.getBoolean(Constants.KEY_ENABLED_KEEP_HOME, false)) {
                Settings.System.putInt(resolver, Constants.HIDE_NAVIGATION_BAR, 0);
            }

            context.startService(new Intent(context, KeepService.class));
        }
    }
}