package com.olivearchi.goodroutine;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TodoDbHelper dbHelper;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;
    private List<SearchResultItem> results = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        setSupportActionBar(findViewById(R.id.toolbar_search));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new TodoDbHelper(this);
        recyclerView = findViewById(R.id.recycler_search_results);
        EditText editQuery = findViewById(R.id.edit_search_query);

        adapter = new SearchAdapter(results, item -> {
            navigateToItem(item);
        });
        recyclerView.setAdapter(adapter);

        editQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        initAds();
    }

    private void performSearch(String query) {
        results.clear();
        adapter.setQuery(query);
        if (query.trim().length() >= 2) {
            results.addAll(dbHelper.searchAll(query));
        }
        adapter.notifyDataSetChanged();
    }

    private void navigateToItem(SearchResultItem result) {
        Intent intent;
        switch (result.getType()) {
            case HABIT:
                // For habits, we navigate to MainActivity but maybe we should go to SecondFragment
                // Since TodoAdapter is in MainActivity (NavHost), we can pass args
                intent = new Intent(this, MainActivity.class);
                // This is a bit tricky since MainActivity is a NavHost. 
                // We'll simplify and go to SelectionActivity then let them pick? 
                // No, let's try to launch specifically.
                // Habit detail is in SecondFragment. 
                // We can't easily launch a Fragment directly from here without messy MainActivity logic.
                // Let's just go to MainActivity and maybe they can find it.
                // Better: If it's a habit, let's just show it.
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

    private void initAds() {
        MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
