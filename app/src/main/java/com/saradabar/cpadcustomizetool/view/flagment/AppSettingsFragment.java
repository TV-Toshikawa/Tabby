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

package com.saradabar.cpadcustomizetool.view.flagment;

import static com.saradabar.cpadcustomizetool.util.Common.isCfmDialog;
import static com.saradabar.cpadcustomizetool.util.Common.isDhizukuActive;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;
import com.saradabar.cpadcustomizetool.view.activity.CrashLogActivity;
import com.saradabar.cpadcustomizetool.view.views.SingleListView;

import java.util.ArrayList;
import java.util.List;

public class AppSettingsFragment extends PreferenceFragmentCompat {

    SwitchPreferenceCompat swUpdateCheck,
            swUseDcha,
            swAdb;

    Preference preCrashLog,
            preDelCrashLog,
            preUpdateMode;

    @Deprecated
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_app, rootKey);

        SharedPreferences sp = requireActivity().getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE);

        swUpdateCheck = findPreference("pre_app_update_check");
        swUseDcha = findPreference("pre_app_use_dcha");
        swAdb = findPreference("pre_app_adb");
        preCrashLog = findPreference("pre_app_crash_log");
        preDelCrashLog = findPreference("pre_app_del_crash_log");
        preUpdateMode = findPreference("pre_app_update_mode");

        swUpdateCheck.setChecked(!Preferences.load(requireActivity(), Constants.KEY_FLAG_UPDATE, true));
        swUseDcha.setChecked(Preferences.load(requireActivity(), Constants.KEY_FLAG_SETTINGS_DCHA, false));

        try {
            swAdb.setChecked(sp.getBoolean(Constants.KEY_ENABLED_AUTO_USB_DEBUG, false));
        } catch (NullPointerException e) {
            sp.edit().putBoolean(Constants.KEY_ENABLED_AUTO_USB_DEBUG, false).apply();
        }

        swUpdateCheck.setOnPreferenceChangeListener((preference, newValue) -> {
            Preferences.save(requireActivity(), Constants.KEY_FLAG_UPDATE, !((boolean) newValue));
            return true;
        });

        swUseDcha.setOnPreferenceChangeListener((preference, newValue) -> {
            Preferences.save(requireActivity(), Constants.KEY_FLAG_SETTINGS_DCHA, (boolean) newValue);
            return true;
        });

        swAdb.setOnPreferenceChangeListener((preference, newValue) -> {
            if (isCfmDialog(requireActivity())) {
                try {
                    if (Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTX || Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTZ)
                        Settings.System.putInt(requireActivity().getContentResolver(), Constants.DCHA_STATE, 3);
                    Thread.sleep(100);
                    Settings.Global.putInt(requireActivity().getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                    if (Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTX || Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTZ)
                        Settings.System.putInt(requireActivity().getContentResolver(), Constants.DCHA_STATE, 0);
                    sp.edit().putBoolean(Constants.KEY_ENABLED_AUTO_USB_DEBUG, (boolean) newValue).apply();
                } catch (SecurityException | InterruptedException ignored) {
                    if (Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTX || Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CTZ)
                        Settings.System.putInt(requireActivity().getContentResolver(), Constants.DCHA_STATE, 0);
                    Toast.toast(requireActivity(), R.string.toast_not_change);
                    swAdb.setChecked(false);
                    return false;
                }
                return true;
            } else {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setMessage("未改造デバイスでは不要なため設定できません")
                        .setPositiveButton(R.string.dialog_common_ok, null)
                        .show();
            }
            return false;
        });

        preCrashLog.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), CrashLogActivity.class));
            return false;
        });

        preDelCrashLog.setOnPreferenceClickListener(preference -> {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setMessage("消去しますか？")
                    .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                        if (Preferences.delete(requireActivity(), Constants.KEY_CRASH_LOG)) {
                            new MaterialAlertDialogBuilder(requireActivity())
                                    .setMessage("消去しました")
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog1, which1) -> dialog1.dismiss())
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        preUpdateMode.setOnPreferenceClickListener(preference -> {
            View v = requireActivity().getLayoutInflater().inflate(R.layout.layout_update_list, null);
            List<SingleListView.AppData> dataList = new ArrayList<>();
            int i = 0;

            for (String str : Constants.list) {
                SingleListView.AppData data = new SingleListView.AppData();
                data.label = str;
                data.updateMode = i;
                dataList.add(data);
                i++;
            }

            ListView listView = v.findViewById(R.id.update_list);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setAdapter(new SingleListView.AppListAdapter(requireActivity(), dataList));
            listView.setOnItemClickListener((parent, mView, position, id) -> {
                switch (position) {
                    case 0:
                        if (Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CT2 || Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CT3) {
                            Preferences.save(requireActivity(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                            listView.invalidateViews();
                        } else {
                            new MaterialAlertDialogBuilder(requireActivity())
                                    .setMessage(getString(R.string.dialog_error_not_work_mode))
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                        break;
                    case 1:
                        Preferences.save(requireActivity(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                        listView.invalidateViews();
                        break;
                    case 2:
                        if (Preferences.load(requireActivity(), Constants.KEY_FLAG_DCHA_SERVICE, false)) {
                            if (MainFragment.getInstance().tryBindDchaService(Constants.FLAG_CHECK, true) && Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) != Constants.MODEL_CT2) {
                                Preferences.save(requireActivity(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                                listView.invalidateViews();
                            } else {
                                new MaterialAlertDialogBuilder(requireActivity())
                                        .setMessage(getString(R.string.dialog_error_not_work_mode))
                                        .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                        .show();
                            }
                        } else {
                            new MaterialAlertDialogBuilder(requireActivity())
                                    .setMessage(getString(R.string.pre_app_sum_confirmation_dcha))
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                        break;
                    case 3:
                        if (((DevicePolicyManager) requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).isDeviceOwnerApp(requireActivity().getPackageName()) && Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) != Constants.MODEL_CT2) {
                            Preferences.save(requireActivity(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                            listView.invalidateViews();
                        } else {
                            new MaterialAlertDialogBuilder(requireActivity())
                                    .setMessage(getString(R.string.dialog_error_not_work_mode))
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                        break;
                    case 4:
                        if (isDhizukuActive(requireActivity())) {
                            Preferences.save(requireActivity(), Constants.KEY_FLAG_UPDATE_MODE, (int) id);
                            listView.invalidateViews();
                        } else {
                            new MaterialAlertDialogBuilder(requireActivity())
                                    .setMessage(getString(R.string.dialog_error_not_work_mode))
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                        break;
                    case 5:
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setMessage(getString(R.string.dialog_error_not_work_mode))
                                .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                                .show();
                }
            });

            new MaterialAlertDialogBuilder(requireActivity())
                    .setView(v)
                    .setTitle(getString(R.string.dialog_title_select_mode))
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> dialog.dismiss())
                    .show();
            return false;
        });

        if (!Preferences.load(requireActivity(), Constants.KEY_FLAG_DCHA_SERVICE, false)) {
            Preferences.save(requireActivity(), Constants.KEY_FLAG_SETTINGS_DCHA, false);
            swUseDcha.setChecked(false);
            swUseDcha.setSummary(getString(R.string.pre_app_sum_confirmation_dcha));
            swUseDcha.setEnabled(false);
        }

        switch (Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2)) {
            case Constants.MODEL_CT2:
            case Constants.MODEL_CT3:
                swAdb.setEnabled(false);
                swAdb.setSummary(Build.MODEL + getString(R.string.pre_main_sum_message_1));
                break;
        }
    }
}