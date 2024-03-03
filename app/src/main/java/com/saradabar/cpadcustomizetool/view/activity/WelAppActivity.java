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

import androidx.fragment.app.Fragment;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.view.flagment.WelcomeFragment;
import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.FragmentWelcomePage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

public class WelAppActivity extends WelcomeActivity {

    @SuppressLint("ResourceAsColor")
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .bottomLayout(WelcomeConfiguration.BottomLayout.INDICATOR_ONLY)
                .defaultBackgroundColor(R.color.white)
                .page(new TitlePage(R.drawable.cpadmaterial, getString(R.string.wel_title_page_1)).titleColor(R.color.black))
                .page(new BasicPage(R.drawable.navigationbar, getString(R.string.wel_title_page_2), getString(R.string.wel_description_page_2)).descriptionColor(R.color.black).headerColor(R.color.black))
                .page(new BasicPage(R.drawable.ex, getString(R.string.wel_title_page_3), getString(R.string.wel_description_page_3)).descriptionColor(R.color.black).headerColor(R.color.black))
                .page(new FragmentWelcomePage() {
                    @Override
                    protected Fragment fragment() {
                        return new WelcomeFragment();
                    }
                })
                .swipeToDismiss(false)
                .build();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }
}