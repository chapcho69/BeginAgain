package com.olivearchi.goodroutine;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class ThemeHelper {
    public static final String PREF_NAME = "ThemePrefs";
    public static final String KEY_THEME = "app_theme";

    public static final int THEME_DEFAULT = 0;
    public static final int THEME_BLACK_WHITE = 1;
    public static final int THEME_SEA = 2;
    public static final int THEME_FOREST = 3;

    public static void applyTheme(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int theme = prefs.getInt(KEY_THEME, THEME_DEFAULT);
        
        switch (theme) {
            case THEME_BLACK_WHITE:
                activity.setTheme(R.style.Theme_GoodRoutine_BlackWhite);
                break;
            case THEME_SEA:
                activity.setTheme(R.style.Theme_GoodRoutine_Sea);
                break;
            case THEME_FOREST:
                activity.setTheme(R.style.Theme_GoodRoutine_Forest);
                break;
            default:
                activity.setTheme(R.style.Theme_GoodRoutine_Default);
                break;
        }
    }

    public static void setTheme(Context context, int theme) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_THEME, theme).apply();
    }
}
