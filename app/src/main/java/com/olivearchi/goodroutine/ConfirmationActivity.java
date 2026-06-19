package com.olivearchi.goodroutine;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConfirmationActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private long todoId;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        todoId = getIntent().getLongExtra("id", -1);
        String subject = getIntent().getStringExtra("subject");
        String detail = getIntent().getStringExtra("detail");
        String time = getIntent().getStringExtra("time");

        TextView titleText = findViewById(R.id.text_confirm_title);
        TextView subjectText = findViewById(R.id.text_confirm_subject);
        TextView detailText = findViewById(R.id.text_confirm_detail);
        TextView timeText = findViewById(R.id.text_confirm_time);

        titleText.setText("[Begin Again]");
        subjectText.setText(getString(R.string.label_title) + " : " + (subject != null ? subject : getString(R.string.title_confirm_none)));
        detailText.setText(getString(R.string.label_content) + " : " + (detail != null ? detail : getString(R.string.title_confirm_none)));
        timeText.setText(getString(R.string.title_confirm_time) + " : " + (time != null ? time : getString(R.string.title_confirm_none)));

        dbHelper = new TodoDbHelper(this);

        Button btnPerformed = findViewById(R.id.btn_performed);
        Button btnNotPerformed = findViewById(R.id.btn_not_performed);

        btnPerformed.setOnClickListener(v -> {
            updateTodoHistory();
            finish();
        });

        btnNotPerformed.setOnClickListener(v -> finish());
        
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

    private void updateTodoHistory() {
        if (todoId == -1) return;

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        dbHelper.addPerformHistory(todoId, timestamp);
        Toast.makeText(this, "수행 기록이 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }
}
