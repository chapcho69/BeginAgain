package com.olivearchi.goodroutine;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class SecretNoteDetailActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private SecretNoteItem item;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_note_detail);

        dbHelper = new TodoDbHelper(this);
        long id = getIntent().getLongExtra("note_id", -1);
        if (id != -1) {
            item = dbHelper.getSecretNoteById(id);
        }

        if (item == null) {
            android.widget.Toast.makeText(this, "항목을 불러올 수 없습니다.", android.widget.Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setSupportActionBar(findViewById(R.id.toolbar_secret_detail));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.feature_secret);
        }

        String title = (item.getTitle() != null) ? item.getTitle() : "";
        String content = (item.getContent() != null) ? item.getContent() : "";
        String remarks = (item.getRemarks() != null) ? item.getRemarks() : "";
        String date = (item.getCreatedAt() != null) ? item.getCreatedAt() : "";

        ((TextView)findViewById(R.id.text_secret_view_title)).setText(SearchHighlightUtils.getHighlightedText(title));
        ((TextView)findViewById(R.id.text_secret_view_content)).setText(SearchHighlightUtils.getHighlightedText(content));
        ((TextView)findViewById(R.id.text_secret_view_remarks)).setText(SearchHighlightUtils.getHighlightedText("비고: " + remarks));
        ((TextView)findViewById(R.id.text_secret_view_date)).setText("작성일: " + date);

        MaterialButton btnEdit = findViewById(R.id.btn_secret_detail_edit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, SecretNoteEditActivity.class);
            intent.putExtra("note_item", item);
            startActivity(intent);
            finish();
        });

        initAds();
    }

    private void initAds() {
        com.google.android.gms.ads.MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.setAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdFailedToLoad(@androidx.annotation.NonNull com.google.android.gms.ads.LoadAdError adError) {
                    adView.setVisibility(android.view.View.GONE);
                }
                @Override
                public void onAdLoaded() {
                    adView.setVisibility(android.view.View.VISIBLE);
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
