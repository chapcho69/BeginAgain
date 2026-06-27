package com.olivearchi.goodroutine;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setSupportActionBar(findViewById(R.id.toolbar_dashboard));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new TodoDbHelper(this);

        setupHeatmap();
        setupReadingStats();
        setupMemoStats();
        setupMemorizationStats();
        initAds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.resume();
    }

    @Override
    protected void onPause() {
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.destroy();
        super.onDestroy();
    }

    private void setupHeatmap() {
        HeatmapView heatmapView = findViewById(R.id.heatmap_routines);
        java.util.Map<String, Integer> counts = dbHelper.getHistoryCountByDate();
        heatmapView.setData(counts);
        
        // Scroll to the end to show the most recent activity
        android.widget.HorizontalScrollView scrollView = findViewById(R.id.scroll_heatmap);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_RIGHT));
    }

    private void setupReadingStats() {
        LinearLayout container = findViewById(R.id.layout_reading_stats);
        java.util.Map<String, Integer> stats = dbHelper.getReadingNoteTotalStats();
        
        Integer books = stats.get("total_books");
        Integer passages = stats.get("total_passages");
        Integer totalChars = stats.get("total_characters");
        
        TextView tv = new TextView(this);
        tv.setTextSize(16);
        
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(R.attr.mainTextColor, typedValue, true);
        tv.setTextColor(typedValue.data);
        
        tv.setLineSpacing(8f, 1f);
        
        String summary = String.format(java.util.Locale.getDefault(), 
                getString(R.string.stats_reading_summary), 
                (books != null ? books : 0), 
                (passages != null ? passages : 0));
        
        if (totalChars != null && totalChars > 0) {
            summary += "\n• 누적 지식량: " + String.format(java.util.Locale.getDefault(), "%,d", totalChars) + "자";
        }
        
        tv.setText(summary);
        container.addView(tv);
    }

    private void setupMemoStats() {
        LinearLayout container = findViewById(R.id.layout_memo_stats);
        drawMonthStats(container, dbHelper.getMemoCountByMonth(), getString(R.string.unit_items));
    }

    private void setupMemorizationStats() {
        LinearLayout container = findViewById(R.id.layout_memorization_stats);
        drawMonthStats(container, dbHelper.getMemorizationCountByMonth(), getString(R.string.unit_items));
    }

    private void drawMonthStats(LinearLayout container, java.util.Map<String, Integer> stats, String unit) {
        List<String> months = new ArrayList<>(stats.keySet());
        Collections.sort(months, Collections.reverseOrder());

        if (months.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText(R.string.msg_no_data);
            container.addView(tv);
            return;
        }

        int max = 0;
        for (int val : stats.values()) if (val > max) max = val;

        for (String month : months) {
            Integer countObj = stats.get(month);
            int count = (countObj != null) ? countObj : 0;
            
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 8, 0, 8);

            TextView label = new TextView(this);
            label.setText(month);
            label.setLayoutParams(new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT));
            
            android.util.TypedValue typedValue = new android.util.TypedValue();
            getTheme().resolveAttribute(R.attr.mainTextColor, typedValue, true);
            label.setTextColor(typedValue.data);
            
            View bar = new View(this);
            int barWidth = (int) ((count / (float) max) * 400); 
            if (barWidth < 10) barWidth = 10;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(barWidth, 40);
            lp.setMargins(16, 0, 16, 0);
            bar.setLayoutParams(lp);
            bar.setBackgroundColor(0xFF90CAF9); // Pastel Blue

            TextView valText = new TextView(this);
            valText.setText(count + unit);
            valText.setTextColor(typedValue.data);

            row.addView(label);
            row.addView(bar);
            row.addView(valText);
            container.addView(row);
        }
    }

    private void initAds() {
        com.google.android.gms.ads.MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
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
            adView.loadAd(new com.google.android.gms.ads.AdRequest.Builder().build());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
