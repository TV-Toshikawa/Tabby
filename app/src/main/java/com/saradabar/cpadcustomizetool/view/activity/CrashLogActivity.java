package com.saradabar.cpadcustomizetool.view.activity;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.saradabar.cpadcustomizetool.R;
import com.saradabar.cpadcustomizetool.util.Constants;
import com.saradabar.cpadcustomizetool.util.Preferences;

public class CrashLogActivity extends AppCompatActivity {

    TextView textView;
    ScrollView scrollView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Deprecated
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crash_log);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        textView = findViewById(R.id.textView);
        scrollView = findViewById(R.id.scrollView);

        if (Preferences.load(this, Constants.KEY_CRASH_LOG, "").length() != 0) {
            addText(Preferences.load(this, Constants.KEY_CRASH_LOG, ""));
        } else {
            addText(getString(R.string.logger_empty));
        }
    }

    private void addText(String status)
    {
        textView.append(status);

        int bottom = textView.getBottom() + scrollView.getPaddingBottom();
        int sy = scrollView.getScrollY();
        int sh = scrollView.getHeight();
        int delta = bottom - (sy + sh);

        scrollView.smoothScrollBy(0, delta);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}