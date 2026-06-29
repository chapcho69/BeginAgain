package com.olivearchi.goodroutine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TextView textLine1;
    private final String line1Source = "Bee's Note";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int index1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        textLine1 = findViewById(R.id.text_splash_line1);
        startTypingAnimation();
    }

    private void startTypingAnimation() {
        animateLine1();
    }

    private void animateLine1() {
        if (index1 <= line1Source.length()) {
            textLine1.setText(line1Source.substring(0, index1));
            index1++;
            handler.postDelayed(this::animateLine1, 80);
        } else {
            handler.postDelayed(() -> {
                if (!isFinishing()) {
                    startActivity(new Intent(SplashActivity.this, SelectionActivity.class));
                    finish();
                }
            }, 800);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
