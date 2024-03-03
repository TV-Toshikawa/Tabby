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

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.data.handler.CrashHandler;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;
import com.saradabar.cpadcustomizetool.util.Toast;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class RebootActivity extends AppCompatActivity {

    IDchaService mDchaService;

    @Deprecated
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));

        if (Preferences.load(this, Constants.KEY_FLAG_DCHA_SERVICE, false)) {
            startReboot();
        } else {
            Toast.toast(this, R.string.toast_use_not_dcha);
            finishAndRemoveTask();
        }
    }

    @Deprecated
    private void startReboot() {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.dialog_question_reboot)
                .setPositiveButton(R.string.dialog_common_yes, (dialog, which) -> {
                    bindService(Constants.DCHA_SERVICE, mDchaServiceConnection, Context.BIND_AUTO_CREATE);
                    Runnable runnable = () -> {
                        try {
                            mDchaService.rebootPad(0, null);
                        } catch (RemoteException ignored) {
                        }
                    };
                    new Handler().postDelayed(runnable, 10);
                })
                .setNegativeButton(R.string.dialog_common_no, (dialog, which) -> finishAndRemoveTask())
                .setOnDismissListener(dialogInterface -> finishAndRemoveTask())
                .show();
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