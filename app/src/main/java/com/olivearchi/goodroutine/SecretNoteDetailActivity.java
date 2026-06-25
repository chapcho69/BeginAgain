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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_note_detail);

        item = (SecretNoteItem) getIntent().getSerializableExtra("note_item");
        if (item == null) { finish(); return; }

        ((TextView)findViewById(R.id.text_secret_view_title)).setText(item.getTitle());
        ((TextView)findViewById(R.id.text_secret_view_content)).setText(item.getContent());
        ((TextView)findViewById(R.id.text_secret_view_remarks)).setText("비고: " + item.getRemarks());
        ((TextView)findViewById(R.id.text_secret_view_date)).setText("작성일: " + item.getCreatedAt());

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
}
