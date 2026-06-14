package com.olivearchi.goodroutine;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Locale;
import java.util.Set;

public class MemorizationDetailActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private MemorizationItem item;
    private TextToSpeech tts;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorization_detail);

        dbHelper = new TodoDbHelper(this);
        item = (MemorizationItem) getIntent().getSerializableExtra("memo_item");

        if (item == null) {
            finish();
            return;
        }

        setSupportActionBar(findViewById(R.id.toolbar_memo_detail));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("암기장 상세");
        }

        ((android.widget.TextView)findViewById(R.id.text_memo_detail_keyword)).setText(item.getKeyword());
        ((android.widget.TextView)findViewById(R.id.text_memo_detail_title)).setText(item.getTitle());
        ((android.widget.TextView)findViewById(R.id.text_memo_detail_content)).setText(item.getContent());
        ((android.widget.TextView)findViewById(R.id.text_memo_detail_date)).setText("최종 수정: " + item.getUpdatedAt());

        MaterialButton btnEdit = findViewById(R.id.btn_memo_detail_edit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, MemorizationEditActivity.class);
            intent.putExtra("memo_item", item);
            startActivity(intent);
            finish();
        });

        MaterialButton btnTts = findViewById(R.id.btn_memo_detail_tts);
        btnTts.setOnClickListener(v -> speakText(item.getContent()));

        MaterialButton btnShare = findViewById(R.id.btn_memo_detail_share);
        btnShare.setOnClickListener(v -> shareContent(item.getTitle(), item.getContent()));

        tts = new TextToSpeech(this, this);
        initAds();
    }

    private void shareContent(String title, String content) {
        String shareText = "제목: " + title + "\n\n" + content + "\n\nshare by Begin Again..";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "내용 공유하기"));
    }

    private void speakText(String text) {
        if (tts != null) {
            float speed = getSharedPreferences("AppPrefs", MODE_PRIVATE).getFloat("ttsSpeed", 1.0f);
            String savedVoiceName = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("ttsVoiceName", "");
            int voiceType = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("ttsVoiceType", 0);
            
            tts.setSpeechRate(speed);
            boolean voiceSet = false;
            Set<Voice> voices = tts.getVoices();
            if (voices != null) {
                if (!savedVoiceName.isEmpty()) {
                    for (Voice v : voices) {
                        if (v.getName().equals(savedVoiceName)) {
                            tts.setVoice(v);
                            voiceSet = true;
                            break;
                        }
                    }
                }
                if (!voiceSet) {
                    for (Voice v : voices) {
                        if (v.getLocale().getLanguage().equals("ko")) {
                            String name = v.getName().toLowerCase();
                            if (voiceType == 1) {
                                if (name.contains("male") || name.contains("남성") || name.contains("-b")) {
                                    tts.setVoice(v); voiceSet = true; break;
                                }
                            } else {
                                if (name.contains("female") || name.contains("여성") || name.contains("-a")) {
                                    tts.setVoice(v); voiceSet = true; break;
                                }
                            }
                        }
                    }
                }
            }
            if (!voiceSet) tts.setPitch(voiceType == 0 ? 1.0f : 0.75f);
            else tts.setPitch(1.0f);
            
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "DetailTTS");
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale.KOREAN);
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

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
