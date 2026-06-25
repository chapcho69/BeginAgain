package com.olivearchi.goodroutine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TodoDbHelper dbHelper;
    private RecyclerView recyclerResults;
    private SearchAdapter resultAdapter;
    private List<SearchResultItem> results = new ArrayList<>();
    
    private RecyclerView recyclerRecent;
    private RecentSearchAdapter recentAdapter;
    private List<String> recentHistory = new ArrayList<>();
    private View layoutRecent;
    private EditText editQuery;

    private static final String PREF_SEARCH = "SearchPrefs";
    private static final String KEY_HISTORY = "recent_history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setSupportActionBar(findViewById(R.id.toolbar_search));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.feature_search);
        }

        dbHelper = new TodoDbHelper(this);
        recyclerResults = findViewById(R.id.recycler_search_results);
        recyclerRecent = findViewById(R.id.recycler_recent_searches);
        layoutRecent = findViewById(R.id.layout_recent_searches);
        editQuery = findViewById(R.id.edit_search_query);

        // Result Adapter
        resultAdapter = new SearchAdapter(results, this::navigateToItem);
        recyclerResults.setAdapter(resultAdapter);

        // Recent Adapter
        loadSearchHistory();
        recentAdapter = new RecentSearchAdapter(recentHistory, new RecentSearchAdapter.OnRecentClickListener() {
            @Override
            public void onRecentClick(String query) {
                editQuery.setText(query);
                editQuery.setSelection(query.length());
                performSearch(query);
            }

            @Override
            public void onDeleteClick(String query) {
                removeSearchHistory(query);
            }
        });
        recyclerRecent.setAdapter(recentAdapter);

        editQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s.toString();
                if (q.trim().isEmpty()) {
                    showRecentLayout(true);
                } else {
                    performSearch(q, false);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        editQuery.setOnEditorActionListener((v, actionId, event) -> {
            String q = editQuery.getText().toString().trim();
            if (!q.isEmpty()) {
                saveSearchHistory(q);
                performSearch(q, true);
            }
            return false;
        });

        initAds();
        showRecentLayout(true);
    }

    private void showRecentLayout(boolean show) {
        layoutRecent.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerResults.setVisibility(show ? View.GONE : View.VISIBLE);
        if (show) {
            loadSearchHistory();
            recentAdapter.notifyDataSetChanged();
        }
    }

    private void performSearch(String query, boolean saveToHistory) {
        showRecentLayout(false);
        results.clear();
        resultAdapter.setQuery(query);
        if (query.trim().length() >= 2) {
            results.addAll(dbHelper.searchAll(query));
            if (saveToHistory) saveSearchHistory(query.trim());
        }
        resultAdapter.notifyDataSetChanged();
    }

    private void performSearch(String query) {
        performSearch(query, false);
    }

    private void loadSearchHistory() {
        SharedPreferences prefs = getSharedPreferences(PREF_SEARCH, MODE_PRIVATE);
        String data = prefs.getString(KEY_HISTORY, "");
        recentHistory.clear();
        if (!data.isEmpty()) {
            recentHistory.addAll(Arrays.asList(data.split("\\|")));
        }
    }

    private void saveSearchHistory(String query) {
        if (query.length() < 2) return;
        
        loadSearchHistory();
        recentHistory.remove(query);
        recentHistory.add(0, query);
        if (recentHistory.size() > 15) {
            recentHistory = recentHistory.subList(0, 15);
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recentHistory.size(); i++) {
            sb.append(recentHistory.get(i));
            if (i < recentHistory.size() - 1) sb.append("|");
        }
        getSharedPreferences(PREF_SEARCH, MODE_PRIVATE).edit().putString(KEY_HISTORY, sb.toString()).apply();
    }

    private void removeSearchHistory(String query) {
        loadSearchHistory();
        recentHistory.remove(query);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < recentHistory.size(); i++) {
            sb.append(recentHistory.get(i));
            if (i < recentHistory.size() - 1) sb.append("|");
        }
        getSharedPreferences(PREF_SEARCH, MODE_PRIVATE).edit().putString(KEY_HISTORY, sb.toString()).apply();
        recentAdapter.notifyDataSetChanged();
    }

    private void navigateToItem(SearchResultItem result) {
        String q = editQuery.getText().toString().trim();
        if (q.length() >= 2) {
            saveSearchHistory(q);
            SearchHighlightUtils.setSearchQuery(q);
        } else {
            SearchHighlightUtils.clearQuery();
        }

        Intent intent;
        switch (result.getType()) {
            case HABIT:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            case READING:
                intent = new Intent(this, ReadingNoteDetailActivity.class);
                intent.putExtra("note_item", (ReadingNoteItem) result.getItem());
                dbHelper.updateReadingNoteAccessTime(((ReadingNoteItem) result.getItem()).getId());
                startActivity(intent);
                break;
            case MEMORIZATION:
                intent = new Intent(this, MemorizationDetailActivity.class);
                intent.putExtra("memo_item", (MemorizationItem) result.getItem());
                dbHelper.updateMemorizationAccessTime(((MemorizationItem) result.getItem()).getId());
                startActivity(intent);
                break;
            case MEMO:
                intent = new Intent(this, MemoDetailActivity.class);
                intent.putExtra("memo_item", (MemoItem) result.getItem());
                dbHelper.updateMemoAccessTime(((MemoItem) result.getItem()).getId());
                startActivity(intent);
                break;
            case TASK:
                intent = new Intent(this, TodayTaskEditActivity.class);
                intent.putExtra("task_item", (TodayTaskItem) result.getItem());
                dbHelper.updateTodayTaskAccessTime(((TodayTaskItem) result.getItem()).getId());
                startActivity(intent);
                break;
            case JAPANESE:
                intent = new Intent(this, JapaneseWordDetailActivity.class);
                intent.putExtra("word_item", (JapaneseWordItem) result.getItem());
                dbHelper.updateJapaneseWordAccessTime(((JapaneseWordItem) result.getItem()).getId());
                startActivity(intent);
                break;
        }
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
        SearchHighlightUtils.clearQuery();
        super.onDestroy();
    }

    private void initAds() {
        com.google.android.gms.ads.MobileAds.initialize(this, status -> {});
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
            com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
