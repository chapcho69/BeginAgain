package com.olivearchi.goodroutine;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;
import java.util.Locale;

public class TodayTaskListActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TodoDbHelper dbHelper;
    private TodayTaskAdapter adapter;
    private RecyclerView recyclerView;
    private TextView totalEstimatedText, totalWorkText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_task_list);

        setSupportActionBar(findViewById(R.id.toolbar_today));

        dbHelper = new TodoDbHelper(this);
        recyclerView = findViewById(R.id.recycler_today_tasks);
        totalEstimatedText = findViewById(R.id.text_total_estimated);
        totalWorkText = findViewById(R.id.text_total_work);

        FloatingActionButton fab = findViewById(R.id.fab_add_today_task);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, TodayTaskEditActivity.class));
        });

        loadTasks();
        initAds();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_home) {
            Intent intent = new Intent(this, SelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        List<TodayTaskItem> tasks = dbHelper.getAllTodayTasks();
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.feature_today) + "(" + tasks.size() + ")");
        } else {
            setTitle(getString(R.string.feature_today) + "(" + tasks.size() + ")");
        }

        int totalEstimated = 0;
        for (TodayTaskItem task : tasks) {
            totalEstimated += task.getEstimatedMinutes();
        }
        
        int totalWork = totalEstimated + (tasks.size() * 10);
        
        totalEstimatedText.setText(getString(R.string.title_task_total_estimated) + ": " + totalEstimated + getString(R.string.title_task_unit_min));
        totalWorkText.setText(getString(R.string.title_task_total_work) + ": " + totalWork + getString(R.string.title_task_unit_min) + " (" + getString(R.string.title_task_item_per) + ")");

        adapter = new TodayTaskAdapter(tasks, item -> {
            dbHelper.updateTodayTaskAccessTime(item.getId());
            Intent intent = new Intent(this, TodayTaskEditActivity.class);
            intent.putExtra("task_item", item);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }
}
