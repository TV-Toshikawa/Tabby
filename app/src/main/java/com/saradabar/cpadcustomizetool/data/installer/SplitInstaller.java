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

package com.saradabar.cpadcustomizetool.data.installer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;

import com.saradabar.cpadcustomizetool.data.service.InstallService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SplitInstaller {

    public static class Result {

        public final boolean bl;

        public Result(boolean result) {
            this.bl = result;
        }
    }

    public static class SessionId {
        public final boolean bl;
        public final int i;

        public SessionId(boolean result, int i) {
            this.bl = result;
            this.i = i;
        }
    }

    public SessionId splitCreateSession(Context context) throws Exception {
        int sessionId = createSession(context.getPackageManager().getPackageInstaller());

        if (sessionId < 0) {
            return new SessionId(false, -1);
        }

        return new SessionId(true, sessionId);
    }

    public Result splitWriteSession(Context context, File apkFile, int sessionId) throws Exception {
        return new Result(writeSession(context.getPackageManager().getPackageInstaller(), sessionId, apkFile));
    }

    public Result splitCommitSession(Context context, int sessionId, int code) {
        return new Result(commitSession(context.getPackageManager().getPackageInstaller(), sessionId, context, code));
    }

    private int createSession(PackageInstaller packageInstaller) throws IOException {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setInstallLocation(PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL);
        return packageInstaller.createSession(params);
    }

    private boolean writeSession(PackageInstaller packageInstaller, int sessionId, File apkFile) throws IOException {
        long sizeBytes = -1;
        String apkPath = apkFile.getAbsolutePath();
        File file = new File(apkPath);

        if (file.isFile()) {
            sizeBytes = file.length();
        }

        PackageInstaller.Session session = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            session = packageInstaller.openSession(sessionId);
            in = new FileInputStream(apkPath);
            out = session.openWrite(getRandomString(), 0, sizeBytes);
            byte[] buffer = new byte[65536];
            int c;

            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }

            session.fsync(out);
            return true;
        } catch (Exception ignored) {
            if (session != null) session.abandon();
            return false;
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
        }
    }

    private boolean commitSession(PackageInstaller packageInstaller, int sessionId, Context context, int code) {
        PackageInstaller.Session session = null;

        try {
            session = packageInstaller.openSession(sessionId);
            Intent intent = new Intent(context, InstallService.class).putExtra("REQUEST_CODE", code).putExtra("REQUEST_SESSION", sessionId);

            PendingIntent pendingIntent = PendingIntent.getService(
                    context,
                    sessionId,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );

            session.commit(pendingIntent.getIntentSender());
            return true;
        } catch (Exception ignored) {
            if (session != null) session.abandon();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    private String getRandomString() {
        String theAlphaNumericS;
        StringBuilder builder;
        theAlphaNumericS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        builder = new StringBuilder(5);

        for (int m = 0; m < 5; m++) {
            int myindex = (int) (theAlphaNumericS.length() * Math.random());
            builder.append(theAlphaNumericS.charAt(myindex));
        }
        return builder.toString();
    }
}