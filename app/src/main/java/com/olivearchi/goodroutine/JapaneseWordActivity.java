package com.olivearchi.goodroutine;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.speech.tts.Voice;

public class JapaneseWordActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TodoDbHelper dbHelper;
    private JapaneseWordAdapter adapter;
    private RecyclerView recyclerView;
    private List<JapaneseWordItem> currentWords;
    private int currentPlayIndex = -1;
    private boolean isPlayingAll = false;
    private boolean isRandomMode = false;
    private List<Integer> shuffleIndices;
    private TextToSpeech tts;
    private AlertDialog autoPlayDialog;
    private int repeatCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_japanese_word);

        setSupportActionBar(findViewById(R.id.toolbar_japanese));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new TodoDbHelper(this);
        recyclerView = findViewById(R.id.recycler_japanese);

        findViewById(R.id.fab_add_japanese).setOnClickListener(v -> {
            startActivity(new Intent(this, JapaneseWordEditActivity.class));
        });

        MaterialButton btnReadAll = findViewById(R.id.btn_japanese_read_all);
        btnReadAll.setOnClickListener(v -> togglePlaySequential());

        MaterialButton btnReadRandom = findViewById(R.id.btn_japanese_read_random);
        btnReadRandom.setOnClickListener(v -> togglePlayRandom());

        tts = new TextToSpeech(this, this);

        loadWords();
        initAds();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.JAPANESE);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String utteranceId) {}
                @Override
                public void onDone(String utteranceId) {
                    if (isPlayingAll && utteranceId.startsWith("Final_")) {
                        runOnUiThread(() -> {
                            if (autoPlayDialog != null) autoPlayDialog.dismiss();
                            playNextWord();
                        });
                    }
                }
                @Override public void onError(String utteranceId) {}
            });
        }
    }

    private int getAdjustedIndex() {
        return currentPlayIndex;
    }

    private void togglePlaySequential() {
        if (isPlayingAll && !isRandomMode) stopPlayback();
        else { isRandomMode = false; startPlayback(); }
    }

    private void togglePlayRandom() {
        if (isPlayingAll && isRandomMode) stopPlayback();
        else { isRandomMode = true; startPlayback(); }
    }

    private void startPlayback() {
        if (currentWords == null || currentWords.isEmpty()) return;
        isPlayingAll = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (isRandomMode) {
            shuffleIndices = new ArrayList<>();
            for (int i = 0; i < currentWords.size(); i++) shuffleIndices.add(i);
            Collections.shuffle(shuffleIndices);
            currentPlayIndex = 0;
        } else {
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            currentPlayIndex = lm != null ? lm.findFirstVisibleItemPosition() : 0;
            if (currentPlayIndex < 0) currentPlayIndex = 0;
        }
        updateButtonStates();
        playNextWord();
    }

    private void stopPlayback() {
        isPlayingAll = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (tts != null) tts.stop();
        if (autoPlayDialog != null) autoPlayDialog.dismiss();
        runOnUiThread(this::updateButtonStates);
    }

    private void updateButtonStates() {
        MaterialButton btnAll = findViewById(R.id.btn_japanese_read_all);
        MaterialButton btnRandom = findViewById(R.id.btn_japanese_read_random);
        if (isPlayingAll) {
            if (isRandomMode) { btnAll.setText("전체 듣기"); btnRandom.setText("중지"); }
            else { btnAll.setText("중지"); btnRandom.setText("무작위 듣기"); }
        } else { btnAll.setText("전체 듣기"); btnRandom.setText("무작위 듣기"); }
    }

    private void playNextWord() {
        if (!isPlayingAll || currentWords == null) { stopPlayback(); return; }
        int wordIndex;
        if (isRandomMode) {
            if (shuffleIndices == null || currentPlayIndex >= shuffleIndices.size()) { stopPlayback(); return; }
            wordIndex = shuffleIndices.get(currentPlayIndex);
            currentPlayIndex += 3;
        } else {
            if (currentPlayIndex >= currentWords.size()) { stopPlayback(); return; }
            wordIndex = currentPlayIndex;
            currentPlayIndex++;
        }
        JapaneseWordItem item = currentWords.get(wordIndex);
        recyclerView.scrollToPosition(wordIndex);
        showAutoPlayOverlay(item);
    }

    private void showAutoPlayOverlay(JapaneseWordItem item) {
        if (autoPlayDialog != null) autoPlayDialog.dismiss();
        View view = getLayoutInflater().inflate(R.layout.activity_japanese_word_detail, null);
        ((TextView)view.findViewById(R.id.text_japanese_word)).setText(item.getWord());
        ((TextView)view.findViewById(R.id.text_japanese_reading)).setText(item.getReading());
        ((TextView)view.findViewById(R.id.text_japanese_meaning)).setText(item.getMeaning());
        ((TextView)view.findViewById(R.id.text_japanese_example)).setText(item.getExample());
        view.findViewById(R.id.layout_japanese_btns).setVisibility(View.GONE);
        autoPlayDialog = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                .setView(view).setCancelable(true).setOnCancelListener(dialog -> stopPlayback()).create();
        autoPlayDialog.show();
        repeatCount = 0;
        startWordSequence(item);
    }

    private void startWordSequence(JapaneseWordItem item) {
        int q = TextToSpeech.QUEUE_FLUSH;
        speakText(item.getWord(), Locale.JAPANESE, q, "Part_W");
        speakText(item.getReading(), Locale.JAPANESE, TextToSpeech.QUEUE_ADD, "Part_R");
        speakText(item.getMeaning(), Locale.KOREAN, TextToSpeech.QUEUE_ADD, "Part_M");
        String finalId = "Final_" + repeatCount;
        if (item.getExample() != null && !item.getExample().isEmpty()) speakText(item.getExample(), Locale.JAPANESE, TextToSpeech.QUEUE_ADD, finalId);
        else speakText(item.getMeaning(), Locale.KOREAN, TextToSpeech.QUEUE_ADD, finalId);
    }

    private void speakText(String text, Locale locale, int mode, String id) {
        if (tts != null) {
            float speed = getSharedPreferences("AppPrefs", MODE_PRIVATE).getFloat("ttsSpeed", 1.0f);
            int voiceType = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("ttsVoiceType", 0);
            tts.setSpeechRate(speed); tts.setLanguage(locale);
            boolean voiceSet = false;
            Set<Voice> voices = tts.getVoices();
            if (voices != null) {
                for (Voice v : voices) {
                    if (v.getLocale().getLanguage().equals(locale.getLanguage())) {
                        String name = v.getName().toLowerCase();
                        if (voiceType == 1) { if (name.contains("male") || name.contains("-b")) { tts.setVoice(v); voiceSet = true; break; } }
                        else { if (name.contains("female") || name.contains("-a")) { tts.setVoice(v); voiceSet = true; break; } }
                    }
                }
            }
            if (!voiceSet) tts.setPitch(voiceType == 0 ? 1.0f : 0.75f); else tts.setPitch(1.0f);
            tts.speak(text, mode, null, id);
        }
    }

    private void loadWords() {
        currentWords = dbHelper.getAllJapaneseWords();
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("일본어(" + currentWords.size() + ")");
        WordDataHolder.setJapaneseWords(currentWords);
        adapter = new JapaneseWordAdapter(currentWords, item -> {
            int index = currentWords.indexOf(item);
            Intent intent = new Intent(this, JapaneseWordDetailActivity.class);
            intent.putExtra("word_index", index);
            intent.putExtra("auto_play", false);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override protected void onResume() { super.onResume(); if (!isPlayingAll) loadWords(); }
    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.menu_list, menu); return true; }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_home) { stopPlayback(); Intent intent = new Intent(this, SelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent); finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
    private void initAds() { MobileAds.initialize(this, status -> {}); AdView adView = findViewById(R.id.adView); if (adView != null) adView.loadAd(new AdRequest.Builder().build()); }
    @Override public boolean onSupportNavigateUp() { stopPlayback(); finish(); return true; }
    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
}
