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

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.saradabar.cpadcustomizetool.R;

import java.util.Objects;

public class WebViewActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Deprecated
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());
        WebView webView = findViewById(R.id.activity_web_view);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("読込中");
                    getSupportActionBar().setSubtitle(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(String.valueOf(view.getTitle()));
                    getSupportActionBar().setSubtitle(url);
                }
            }

            @Deprecated
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isUrlDistrusted(url)) {
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("接続拒否");
                        getSupportActionBar().setSubtitle("アクセスしようとしたサイトは非SSL接続のため拒否しました");
                    }
                }
                return isUrlDistrusted(url);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(false);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        webView.getSettings().setAllowFileAccess(false);

        if (getIntent().getStringExtra("URL") != null) {
            webView.loadUrl(Objects.requireNonNull(getIntent().getStringExtra("URL")));
        } else {
            webView.loadUrl("https://www.google.com");
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                webView.goBack();
            }
        });
    }

    private boolean isUrlDistrusted(String url) {
        try {
            return !Objects.equals(Uri.parse(url).getScheme(), "https");
        } catch (Exception ignored) {
        }
        return true;
    }
}
