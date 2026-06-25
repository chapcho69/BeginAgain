package com.olivearchi.goodroutine;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordMapActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TodoDbHelper dbHelper;
    private WordMapView wordMapView;
    private ProgressBar progressBar;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_map);

        setSupportActionBar(findViewById(R.id.toolbar_word_map));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new TodoDbHelper(this);
        wordMapView = findViewById(R.id.word_map_view);
        progressBar = findViewById(R.id.progress_word_map);
        emptyText = findViewById(R.id.text_word_map_empty);

        findViewById(R.id.btn_filter_all).setOnClickListener(v -> loadAndProcessData(null));
        findViewById(R.id.btn_filter_month).setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.MONTH, -1);
            String since = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());
            loadAndProcessData(since);
        });

        initAds();
        loadAndProcessData(null);
    }

    private void initAds() {
        com.google.android.gms.ads.MobileAds.initialize(this, initializationStatus -> {});
        com.google.android.gms.ads.AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.setAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdFailedToLoad(@androidx.annotation.NonNull com.google.android.gms.ads.LoadAdError adError) {
                    adView.setVisibility(View.GONE);
                }
                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }
            });
            com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    private void loadAndProcessData(String sinceDate) {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        new Thread(() -> {
            List<String> contents = dbHelper.getAllRawContents(sinceDate);
            Map<String, Integer> freqMap = new HashMap<>();

            for (String text : contents) {
                if (text == null) continue;
                // Split by anything that is not a letter or digit
                String[] words = text.split("[^a-zA-Z0-9가-힣]+");
                for (String w : words) {
                    String clean = cleanWord(w);
                    // Only include words that are at least 2 characters long and actually contain letters
                    if (clean.length() >= 2 && clean.matches(".*[a-zA-Z가-힣].*")) {
                        freqMap.put(clean, freqMap.getOrDefault(clean, 0) + 1);
                    }
                }
            }

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (freqMap.isEmpty()) {
                    emptyText.setVisibility(View.VISIBLE);
                } else {
                    wordMapView.setWords(freqMap);
                }
            });
        }).start();
    }

    private String cleanWord(String w) {
        String clean = w.trim();
        // Remove common Korean particles (very basic filtering)
        String[] particles = {"은", "는", "이", "가", "을", "를", "의", "에", "로", "으로", "에서"};
        for (String p : particles) {
            if (clean.endsWith(p) && clean.length() > p.length()) {
                return clean.substring(0, clean.length() - p.length());
            }
        }
        return clean;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
