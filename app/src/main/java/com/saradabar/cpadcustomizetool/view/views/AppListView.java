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

package com.saradabar.cpadcustomizetool.view.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;

import java.util.List;

public class AppListView {

    public static class AppData {
        public String str;
    }

    public static class AppListAdapter extends ArrayAdapter<AppListView.AppData> {

        private final LayoutInflater mInflater;

        public AppListAdapter(Context context, List<AppListView.AppData> dataList) {
            super(context, R.layout.view_app_list_item);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(dataList);
        }

        @Deprecated
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            AppListView.ViewHolder holder = new AppListView.ViewHolder();

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_app_list_item, parent, false);
                holder.tv = convertView.findViewById(R.id.v_app_list_text);
                convertView.setTag(holder);
            } else {
                holder = (AppListView.ViewHolder) convertView.getTag();
            }

            final AppListView.AppData data = getItem(position);

            holder.tv.setText(data.str);

            /* RadioButtonの更新 */
            RadioButton button = convertView.findViewById(R.id.v_app_list_radio);
            button.setChecked(Preferences.load(getContext(), Constants.KEY_RADIO_TMP, 0) == position);

            return convertView;
        }
    }

    public static class ViewHolder {
        TextView tv;
    }
}
