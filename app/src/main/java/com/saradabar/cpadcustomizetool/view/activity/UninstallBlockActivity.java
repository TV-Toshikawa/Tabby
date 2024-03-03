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

import static com.saradabar.cpadcustomizetool.util.Common.isDhizukuActive;
import static com.saradabar.cpadcustomizetool.util.Common.mDhizukuService;
import static com.saradabar.cpadcustomizetool.util.Common.tryBindDhizukuService;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.Receiver.AdministratorReceiver;

import java.util.ArrayList;
import java.util.List;

public class UninstallBlockActivity extends AppCompatActivity {

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_uninstall_list);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        DevicePolicyManager dpm = (DevicePolicyManager) this.getSystemService("device_policy");
        final PackageManager pm = getPackageManager();
        final List<ApplicationInfo> installedAppList = pm.getInstalledApplications(0);
        final List<AppData> dataList = new ArrayList<>();

        for (ApplicationInfo app : installedAppList) {
            /* ユーザーアプリか確認 */
            if (app.sourceDir.startsWith("/data/app/")) {
                AppData data = new AppData();
                data.label = app.loadLabel(pm).toString();
                data.icon = app.loadIcon(pm);
                data.packName = app.packageName;
                dataList.add(data);
            }
        }

        final ListView listView = findViewById(R.id.un_list);
        Button unDisableButton = findViewById(R.id.un_button_disable);
        Button unEnableButton = findViewById(R.id.un_button_enable);

        listView.setAdapter(new AppListAdapter(this, dataList));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            AppData item = dataList.get(position);
            String selectPackage = Uri.fromParts("package", item.packName, null).toString();
            if (isDhizukuActive(this)) {
                if (tryBindDhizukuService(this)) {
                    try {
                        mDhizukuService.setUninstallBlocked(selectPackage.replace("package:", ""), !mDhizukuService.isUninstallBlocked(selectPackage.replace("package:", "")));
                    } catch (RemoteException ignored) {
                    }
                }
            } else {
                dpm.setUninstallBlocked(new ComponentName(this, AdministratorReceiver.class), selectPackage.replace("package:", ""), !dpm.isUninstallBlocked(new ComponentName(this, AdministratorReceiver.class), selectPackage.replace("package:", "")));
            }
            /* listviewの更新 */
            listView.invalidateViews();
        });

        /* ボタンが押されたならスイッチ一括変更 */
        /* 無効 */
        unDisableButton.setOnClickListener(v -> {
            if (isDhizukuActive(this)) {
                if (tryBindDhizukuService(this)) {
                    try {
                        for (AppData appData : dataList) {
                            mDhizukuService.setUninstallBlocked(appData.packName, false);
                        }
                        ((MaterialSwitch) AppListAdapter.view.findViewById(R.id.un_switch)).setChecked(false);
                    } catch (RemoteException ignored) {
                    }
                }
            } else {
                for (AppData appData : dataList) {
                    dpm.setUninstallBlocked(new ComponentName(this, AdministratorReceiver.class), appData.packName, false);
                }
                ((MaterialSwitch) AppListAdapter.view.findViewById(R.id.un_switch)).setChecked(false);
            }
            /* listviewの更新 */
            listView.invalidateViews();
        });

        /* 有効 */
        unEnableButton.setOnClickListener(v -> {
            if (isDhizukuActive(this)) {
                if (tryBindDhizukuService(this)) {
                    try {
                        for (AppData appData : dataList) {
                            mDhizukuService.setUninstallBlocked(appData.packName, true);
                        }
                        ((MaterialSwitch) AppListAdapter.view.findViewById(R.id.un_switch)).setChecked(true);
                    } catch (RemoteException ignored) {
                    }
                }
            } else {
                for (AppData appData : dataList) {
                    dpm.setUninstallBlocked(new ComponentName(this, AdministratorReceiver.class), appData.packName, true);
                }
                ((MaterialSwitch) AppListAdapter.view.findViewById(R.id.un_switch)).setChecked(true);
            }
            /* listviewの更新 */
            listView.invalidateViews();
        });
    }

    private static class AppData {
        String label;
        Drawable icon;
        String packName;
    }

    private static class AppListAdapter extends ArrayAdapter<AppData> {

        private final LayoutInflater mInflater;

        private final DevicePolicyManager dpm;

        @SuppressLint("StaticFieldLeak")
        public static View view;

        public AppListAdapter(Context context, List<AppData> dataList) {
            super(context, R.layout.view_uninstall_item);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            dpm = (DevicePolicyManager) context.getSystemService(DEVICE_POLICY_SERVICE);
            addAll(dataList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = new ViewHolder();

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_uninstall_item, parent, false);
                holder.textLabel = convertView.findViewById(R.id.un_label);
                holder.imageIcon = convertView.findViewById(R.id.un_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            view = convertView;

            final AppData data = getItem(position);

            holder.textLabel.setText(data.label);
            holder.imageIcon.setImageDrawable(data.icon);

            if (isDhizukuActive(getContext())) {
                if (tryBindDhizukuService(getContext())) {
                    try {
                        ((MaterialSwitch) convertView.findViewById(R.id.un_switch)).setChecked(mDhizukuService.isUninstallBlocked(data.packName));
                    } catch (Exception ignored) {
                    }
                }
            } else {
                ((MaterialSwitch) convertView.findViewById(R.id.un_switch)).setChecked(dpm.isUninstallBlocked(new ComponentName(getContext(), AdministratorReceiver.class), data.packName));
            }
            return convertView;
        }
    }

    private static class ViewHolder {
        TextView textLabel;
        ImageView imageIcon;
    }

    /* メニュー選択 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}