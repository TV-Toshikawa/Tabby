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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

public class Preferences {

    /* データ管理 */
    @Deprecated
    public static void save(Context context, String key, int value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply();
    }

    @Deprecated
    public static void save(Context context, String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply();
    }

    @Deprecated
    public static void save(Context context, String key, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply();
    }

    @Deprecated
    public static int load(Context context, String key, int value) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, value);
    }

    @Deprecated
    public static boolean load(Context context, String key, boolean value) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, value);
    }

    @Deprecated
    public static String load(Context context, String key, String value) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, value);
    }

    @Deprecated
    public static boolean delete(Context context, String key) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(key).apply();
        return true;
    }

    /* マルチリストのデータ取得 */
    public static Set<String> getEmergencySettings(Context context) {
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet(Constants.KEY_EMERGENCY_SETTINGS, null);
    }

    public static boolean isEmergencySettingsDchaState(Context context) {
        Set<String> set = getEmergencySettings(context);

        if (set != null) {
            return set.contains(Integer.toString(1));
        }
        return false;
    }

    public static boolean isEmergencySettingsNavigationBar(Context context) {
        Set<String> set = getEmergencySettings(context);

        if (set != null) {
            return set.contains(Integer.toString(2));
        }
        return false;
    }

    public static boolean isEmergencySettingsLauncher(Context context) {
        Set<String> set = getEmergencySettings(context);

        if (set != null) {
            return set.contains(Integer.toString(3));
        }
        return false;
    }

    public static boolean isEmergencySettingsRemoveTask(Context context) {
        Set<String> set = getEmergencySettings(context);

        if (set != null) {
            return set.contains(Integer.toString(4));
        }
        return false;
    }

    private static Set<String> getNormalModeSettings(Context context) {
        SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getStringSet(Constants.KEY_NORMAL_SETTINGS, null);
    }

    public static boolean isNormalModeSettingsDchaState(Context context) {
        Set<String> set = getNormalModeSettings(context);

        if (set != null) {
            return set.contains(Integer.toString(1));
        }
        return false;
    }

    public static boolean isNormalModeSettingsNavigationBar(Context context) {
        Set<String> set = getNormalModeSettings(context);

        if (set != null) {
            return set.contains(Integer.toString(2));
        }
        return false;
    }

    public static boolean isNormalModeSettingsLauncher(Context context) {
        Set<String> set = getNormalModeSettings(context);

        if (set != null) {
            return set.contains(Integer.toString(3));
        }
        return false;
    }

    public static boolean isNormalModeSettingsActivity(Context context) {
        Set<String> set = getNormalModeSettings(context);

        if (set != null) {
            return set.contains(Integer.toString(4));
        }
        return false;
    }
}