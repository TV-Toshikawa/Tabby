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

package com.saradabar.cpadcustomizetool.data.connection;

import android.app.Activity;
import android.os.AsyncTask;

import com.saradabar.cpadcustomizetool.data.event.DownloadEventListener;
import com.saradabar.cpadcustomizetool.data.event.DownloadEventListenerList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

@Deprecated
public class AsyncFileDownload extends AsyncTask<String, Void, Boolean> {

	DownloadEventListenerList downloadEventListenerList;
	String url;
	int reqCode;
	File outputFile;
	FileOutputStream fileOutputStream;
	BufferedInputStream bufferedInputStream;
	int totalByte = 0, currentByte = 0;

	@Deprecated
	public AsyncFileDownload(Activity activity, String str, File file, int i) {
		url = str;
		outputFile = file;
		reqCode = i;
		downloadEventListenerList = new DownloadEventListenerList();
		downloadEventListenerList.addEventListener((DownloadEventListener) activity);
	}

	@Deprecated
	@Override
	protected Boolean doInBackground(String... str) {
		final byte[] buffer = new byte[1024];

		try {
			HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
			httpURLConnection.setReadTimeout(20000);
			httpURLConnection.setConnectTimeout(20000);
			bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream(), 1024);
			fileOutputStream = new FileOutputStream(outputFile);
			totalByte = httpURLConnection.getContentLength();
		} catch (SocketTimeoutException | MalformedURLException ignored) {
			return false;
		} catch (IOException ignored) {
			return null;
		}

		if (isCancelled()) {
			return false;
		}

		try {
			int len;

			while ((len = bufferedInputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, len);
				currentByte += len;

				if (isCancelled()) break;
			}
		} catch (IOException ignored) {
			return false;
		}

		try {
			close();
		} catch (IOException ignored) {
		}

		return true;
	}

	@Deprecated
	@Override
	protected void onPreExecute() {
	}

	@Deprecated
	@Override
	protected void onPostExecute(Boolean result) {
		if (result != null) {
			if (result) downloadEventListenerList.downloadCompleteNotify(reqCode);
			else downloadEventListenerList.downloadErrorNotify(reqCode);
		} else downloadEventListenerList.connectionErrorNotify(reqCode);
	}

	@Deprecated
	@Override
	protected void onProgressUpdate(Void... progress) {
	}

	private void close() throws IOException {
		fileOutputStream.flush();
		fileOutputStream.close();
		bufferedInputStream.close();
	}

	public int getLoadedBytePercent() {
		if (totalByte <= 0) return 0;
		return (int) Math.floor(100 * currentByte / totalByte);
	}
}