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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;
import com.saradabar.cpadcustomizetool.view.activity.WebViewActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OtherFragment extends PreferenceFragmentCompat {

    Preference preOtherStartSettings,
            preStartUiAdjustment,
            preStartDevSettings,
            preScreenOffTimeOut,
            preWebView;

    @Deprecated
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pre_other, rootKey);

        preOtherStartSettings = findPreference("pre_other_start_settings");
        preStartUiAdjustment = findPreference("pre_other_start_ui_adjustment");
        preStartDevSettings = findPreference("pre_other_start_dev_settings");
        preScreenOffTimeOut = findPreference("pre_other_screen_off_time");
        preWebView = findPreference("pre_other_web_view");

        preOtherStartSettings.setOnPreferenceClickListener(preference -> {
            try {
                startActivity(new Intent().setClassName("com.android.settings", "com.android.settings.Settings").addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preStartDevSettings.setOnPreferenceClickListener(preference -> {
            if (Settings.Secure.getInt(requireActivity().getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1) {
                try {
                    if (!Constants.COUNT_DCHA_COMPLETED_FILE.exists()) {
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        return false;
                    } else if (Constants.COUNT_DCHA_COMPLETED_FILE.exists()) {
                        if (Settings.System.getInt(requireActivity().getContentResolver(), Constants.DCHA_STATE, 0) != 0) {
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        } else {
                            Settings.System.putInt(requireActivity().getContentResolver(), Constants.DCHA_STATE, 3);
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                        }
                    }
                } catch (ActivityNotFoundException ignored) {
                }
            } else {
                Toast.toast(getActivity(), R.string.toast_no_development_option);
            }
            return false;
        });

        preStartUiAdjustment.setOnPreferenceClickListener(preference -> {
            try {
                startActivity(new Intent().setClassName("com.android.systemui", "com.android.systemui.DemoMode").addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            } catch (ActivityNotFoundException ignored) {
            }
            return false;
        });

        preScreenOffTimeOut.setOnPreferenceClickListener(preference -> {
            View view = requireActivity().getLayoutInflater().inflate(R.layout.view_time_out, null);
            Button button = view.findViewById(R.id.time_out_button);
            EditText editText = view.findViewById(R.id.time_out_edit);
            editText.setHint(getString(R.string.layout_time_out_hint, String.valueOf(Integer.MAX_VALUE)));
            setTextScreenOffTimeConvert(view.findViewById(R.id.time_out_label));
            new MaterialAlertDialogBuilder(requireActivity())
                    .setView(view)
                    .setCancelable(false)
                    .setTitle("スクリーンのタイムアウト")
                    .setPositiveButton(R.string.dialog_common_ok, (dialog, which) -> {
                        ((InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editText.getWindowToken(), 0);
                        try {
                            if (Integer.parseInt(editText.getText().toString()) >= 5000) {
                                Settings.System.putInt(requireActivity().getContentResolver(), "screen_off_timeout", Integer.parseInt(editText.getText().toString()));
                                setSummaryScreenOffTimeConvert();
                            } else {
                                new MaterialAlertDialogBuilder(requireActivity())
                                        .setMessage("最低値5000以下の値を設定することはできません")
                                        .setPositiveButton(R.string.dialog_common_ok, (dialog1, which1) -> dialog1.dismiss())
                                        .show();
                            }
                        } catch (Exception ignored) {
                            new MaterialAlertDialogBuilder(requireActivity())
                                    .setMessage(R.string.dialog_error)
                                    .setPositiveButton(R.string.dialog_common_ok, (dialog1, which1) -> dialog1.dismiss())
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.dialog_common_cancel, null)
                    .show();
            button.setOnClickListener(view1 -> {
                ((InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editText.getWindowToken(), 0);
                try {
                    editText.setText(String.valueOf(Integer.MAX_VALUE));
                    Settings.System.putInt(requireActivity().getContentResolver(), "screen_off_timeout", Integer.MAX_VALUE);
                    setTextScreenOffTimeConvert(view.findViewById(R.id.time_out_label));
                    setSummaryScreenOffTimeConvert();
                } catch (Exception ignored) {
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(R.string.dialog_error)
                            .setPositiveButton(R.string.dialog_common_ok, (dialog1, which1) -> dialog1.dismiss())
                            .show();
                }
            });
            Runnable runnable = () -> ((InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editText.getWindowToken(), 0);
            new Handler().postDelayed(runnable, 10);
            return false;
        });

        preWebView.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), WebViewActivity.class).putExtra("URL", "https://www.google.com").addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            return false;
        });

        if (Preferences.load(requireActivity(), Constants.KEY_MODEL_NAME, Constants.MODEL_CT2) == Constants.MODEL_CT2) {
            preStartUiAdjustment.setEnabled(false);
            preStartUiAdjustment.setSummary(Build.MODEL + "ではこの機能は使用できません");
        }
        setSummaryScreenOffTimeConvert();
    }

    /* 再表示 */
    @Override
    public void onResume() {
        super.onResume();
        setSummaryScreenOffTimeConvert();
    }

    private void setTextScreenOffTimeConvert(TextView textView) {
        long time, sec, min, hour, day;

        time = Settings.System.getInt(requireActivity().getContentResolver(), "screen_off_timeout", 60) / 1000;
        sec = time % 60;
        min = (time / 60) % 60;
        hour = (time / 3600) % 24;
        day = (time / 86400) % 31;
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        calendar.add(Calendar.SECOND, (int) time);

        date = calendar.getTime();
        DateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.JAPAN);

        textView.setText(getString(R.string.layout_time_out_label, Settings.System.getInt(requireActivity().getContentResolver(), "screen_off_timeout", 60) + "（" + time + "）", day + "日" + hour + "時間" + min + "分" + sec + "秒", df.format(date)));
    }

    private void setSummaryScreenOffTimeConvert() {
        long time, sec, min, hour, day;

        time = Settings.System.getInt(requireActivity().getContentResolver(), "screen_off_timeout", 60) / 1000;
        sec = time % 60;
        min = (time / 60) % 60;
        hour = (time / 3600) % 24;
        day = (time / 86400) % 31;

        preScreenOffTimeOut.setSummary("操作が行われない状態で" + day + "日" + hour + "時間" + min + "分" + sec + "秒" + "経過後");
    }
}