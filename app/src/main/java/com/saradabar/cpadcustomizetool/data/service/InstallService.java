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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.IBinder;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.data.connection.Updater;
import com.saradabar.cpadcustomizetool.data.event.InstallEventListenerList;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.view.activity.StartActivity;

public class InstallService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        postStatus(intent.getIntExtra("REQUEST_SESSION", -1), intent.getIntExtra("REQUEST_CODE", 0), intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1), intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
        stopSelf();

        return START_NOT_STICKY;
    }

    private void postStatus(int sessionId, int code, int status, String extra) {
        InstallEventListenerList installEventListener = new InstallEventListenerList();

        switch (code) {
            case Constants.REQUEST_INSTALL_SILENT:
            case Constants.REQUEST_INSTALL_GET_APP:
                installEventListener.addEventListener(StartActivity.getInstance());
                break;
            case Constants.REQUEST_INSTALL_SELF_UPDATE:
                installEventListener.addEventListener(Updater.getInstance());
                break;
        }

        switch (status) {
            case PackageInstaller.STATUS_SUCCESS:
                try {
                    getPackageManager().getPackageInstaller().openSession(sessionId).close();
                } catch (Exception ignored) {
                }
                installEventListener.installSuccessNotify(code);
                break;
            case PackageInstaller.STATUS_FAILURE_ABORTED:
                try {
                    getPackageManager().getPackageInstaller().openSession(sessionId).abandon();
                } catch (Exception ignored) {
                }
                installEventListener.installFailureNotify(code, getErrorMessage(this, status) + "\n" + extra);
                break;
            default:
                try {
                    getPackageManager().getPackageInstaller().openSession(sessionId).abandon();
                } catch (Exception ignored) {
                }
                installEventListener.installErrorNotify(code, getErrorMessage(this, status) + "\n" + extra);
                break;
        }
    }

    private String getErrorMessage(Context context, int status) {
        switch (status) {
            case PackageInstaller.STATUS_FAILURE_ABORTED:
                return context.getString(R.string.installer_status_user_action);
            case PackageInstaller.STATUS_FAILURE_BLOCKED:
                return context.getString(R.string.installer_status_failure_blocked);
            case PackageInstaller.STATUS_FAILURE_CONFLICT:
                return context.getString(R.string.installer_status_failure_conflict);
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                return context.getString(R.string.installer_status_failure_incompatible);
            case PackageInstaller.STATUS_FAILURE_INVALID:
                return context.getString(R.string.installer_status_failure_invalid);
            case PackageInstaller.STATUS_FAILURE_STORAGE:
                return context.getString(R.string.installer_status_failure_storage);
            default:
                return context.getString(R.string.installer_status_failure);
        }
    }
}