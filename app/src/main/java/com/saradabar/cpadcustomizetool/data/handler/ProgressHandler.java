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

package com.saradabar.cpadcustomizetool.data.handler;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.saradabar.cpadcustomizetool.data.connection.AsyncFileDownload;
import com.saradabar.cpadcustomizetool.view.flagment.MainFragment;

@Deprecated
public class ProgressHandler extends Handler {

    public LinearProgressIndicator linearProgressIndicator;
    public TextView textView;
    public AsyncFileDownload asyncfiledownload;

    @Deprecated
    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);

        if (asyncfiledownload.isCancelled()) {
            linearProgressIndicator.setProgress(0);
            linearProgressIndicator.setIndeterminate(true);
            linearProgressIndicator.hide();
        } else if (asyncfiledownload.getStatus() == AsyncTask.Status.FINISHED) {
            linearProgressIndicator.setProgress(0);
            linearProgressIndicator.setIndeterminate(true);
        } else {
            linearProgressIndicator.setIndeterminate(false);
            linearProgressIndicator.setProgress(asyncfiledownload.getLoadedBytePercent());
            try {
                MainFragment.getInstance().preGetApp.setSummary("インストールファイルをサーバーからダウンロードしています...しばらくお待ち下さい...\n進行状況：" + asyncfiledownload.getLoadedBytePercent() + "%");
            } catch (Exception ignored) {
            }

            try {
                textView.setText("インストールファイルをサーバーからダウンロードしています...しばらくお待ち下さい...\n進行状況：" + asyncfiledownload.getLoadedBytePercent() + "%");
            } catch (Exception ignored) {
            }
            sendEmptyMessageDelayed(0, 100);
        }
    }
}