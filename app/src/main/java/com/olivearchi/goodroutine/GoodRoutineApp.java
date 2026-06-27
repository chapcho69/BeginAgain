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
    }
}
