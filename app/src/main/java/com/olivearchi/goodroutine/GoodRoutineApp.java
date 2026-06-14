package com.olivearchi.goodroutine;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class GoodRoutineApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Force Light Mode globally at the application level
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
