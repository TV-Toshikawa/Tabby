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