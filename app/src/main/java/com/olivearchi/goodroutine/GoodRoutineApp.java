package com.olivearchi.goodroutine;

import android.app.Application;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.play.core.splitcompat.SplitCompat;

public class GoodRoutineApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        // Must install SplitCompat before super.attachBaseContext to ensure all splits are ready
        SplitCompat.install(base);
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        initializeDefaultSettings();
    }

    private void initializeDefaultSettings() {
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (!prefs.contains("appName")) {
            // First run: Set default localized app name
            String defaultName = getString(R.string.app_name);
            prefs.edit().putString("appName", defaultName).apply();
        }
    }
}
