package com.olivearchi.goodroutine;

import android.content.Intent;
import android.media.MediaPlayer;
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
    private TextView textLine2;
    private TextView textLine3;

    private final String line1Source = "Begin Again";
    private final String line2Source = "제자리를 맴도는 나사처럼";
    private final String line3Source = "매일 반복으로 쌓이는 힘";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private MediaPlayer mediaPlayer;
    
    private int index1 = 0;
    private int index2 = 0;
    private int index3 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        textLine1 = findViewById(R.id.text_splash_line1);
        textLine2 = findViewById(R.id.text_splash_line2);
        textLine3 = findViewById(R.id.text_splash_line3);

        mediaPlayer = MediaPlayer.create(this, R.raw.mouse_click_01);

        startTypingAnimation();
    }

    private void playTypingSound() {
        // Sound effect removed as requested
    }

    private void startTypingAnimation() {
        animateLine1();
    }

    private void animateLine1() {
        if (index1 <= line1Source.length()) {
            textLine1.setText(line1Source.substring(0, index1));
            if (index1 > 0 && index1 % 5 == 0) playTypingSound();
            index1++;
            handler.postDelayed(this::animateLine1, 60);
        } else {
            handler.postDelayed(this::animateLine2, 150);
        }
    }

    private void animateLine2() {
        if (index2 <= line2Source.length()) {
            textLine2.setText(line2Source.substring(0, index2));
            if (index2 > 0 && index2 % 5 == 0) playTypingSound();
            index2++;
            handler.postDelayed(this::animateLine2, 70);
        } else {
            handler.postDelayed(this::animateLine3, 150);
        }
    }

    private void animateLine3() {
        if (index3 <= line3Source.length()) {
            textLine3.setText(line3Source.substring(0, index3));
            if (index3 > 0 && index3 % 5 == 0) playTypingSound();
            index3++;
            handler.postDelayed(this::animateLine3, 70);
        } else {
            handler.postDelayed(() -> {
                if (!isFinishing()) {
                    startActivity(new Intent(SplashActivity.this, SelectionActivity.class));
                    finish();
                }
            }, 500);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
