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

public class EnglishWordActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TodoDbHelper dbHelper;
    private EnglishWordAdapter adapter;
    private RecyclerView recyclerView;
    private List<EnglishWordItem> currentWords;
    private int currentPlayIndex = -1;
    private boolean isPlayingAll = false;
    private boolean isRandomMode = false;
    private List<Integer> shuffleIndices;
    private TextToSpeech tts;
    private AlertDialog autoPlayDialog;
    private int repeatCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_english_word);

        setSupportActionBar(findViewById(R.id.toolbar_english));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new TodoDbHelper(this);
        recyclerView = findViewById(R.id.recycler_english);

        findViewById(R.id.fab_add_english).setOnClickListener(v -> {
            startActivity(new Intent(this, EnglishWordEditActivity.class));
        });

        MaterialButton btnReadAll = findViewById(R.id.btn_read_all);
        btnReadAll.setOnClickListener(v -> togglePlaySequential());

        MaterialButton btnReadRandom = findViewById(R.id.btn_read_random);
        btnReadRandom.setOnClickListener(v -> togglePlayRandom());

        tts = new TextToSpeech(this, this);

        loadWords();
        initAds();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
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
        MaterialButton btnAll = findViewById(R.id.btn_read_all);
        MaterialButton btnRandom = findViewById(R.id.btn_read_random);
        if (isPlayingAll) {
            if (isRandomMode) { btnAll.setText(R.string.btn_tts_all); btnRandom.setText(R.string.btn_stop); }
            else { btnAll.setText(R.string.btn_stop); btnRandom.setText(R.string.btn_tts_random); }
        } else { btnAll.setText(R.string.btn_tts_all); btnRandom.setText(R.string.btn_tts_random); }
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
        EnglishWordItem item = currentWords.get(wordIndex);
        recyclerView.scrollToPosition(wordIndex);
        showAutoPlayOverlay(item);
    }

    private void showAutoPlayOverlay(EnglishWordItem item) {
        if (autoPlayDialog != null) autoPlayDialog.dismiss();
        View view = getLayoutInflater().inflate(R.layout.activity_english_word_detail, null);
        ((TextView)view.findViewById(R.id.text_english_word)).setText(item.getWord());
        ((TextView)view.findViewById(R.id.text_english_meaning)).setText(item.getMeaning());
        ((TextView)view.findViewById(R.id.text_english_example)).setText(item.getExample());
        view.findViewById(R.id.layout_english_btns).setVisibility(View.GONE);
        autoPlayDialog = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                .setView(view).setCancelable(true).setOnCancelListener(dialog -> stopPlayback()).create();
        autoPlayDialog.show();
        repeatCount = 0;
        startWordSequence(item);
    }

    private void startWordSequence(EnglishWordItem item) {
        int q = TextToSpeech.QUEUE_FLUSH;
        speakText(item.getWord(), Locale.US, q, "Part_W");
        speakText(item.getMeaning(), Locale.KOREAN, TextToSpeech.QUEUE_ADD, "Part_M");
        String finalId = "Final_" + repeatCount;
        if (item.getExample() != null && !item.getExample().isEmpty()) speakText(item.getExample(), Locale.US, TextToSpeech.QUEUE_ADD, finalId);
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
        currentWords = dbHelper.getAllEnglishWords();
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(getString(R.string.feature_english) + "(" + currentWords.size() + ")");
        WordDataHolder.setEnglishWords(currentWords);
        adapter = new EnglishWordAdapter(currentWords, item -> {
            int index = currentWords.indexOf(item);
            Intent intent = new Intent(this, EnglishWordDetailActivity.class);
            intent.putExtra("word_index", index);
            intent.putExtra("auto_play", false);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.resume();
        if (!isPlayingAll) loadWords();
    }

    @Override
    protected void onPause() {
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.pause();
        // 전체 듣기 중에는 화면이 전환되어도 중지하지 않고 TTS를 계속 유지합니다.
        super.onPause();
    }

    private void initAds() {
        com.google.android.gms.ads.MobileAds.initialize(this, status -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.loadAd(new com.google.android.gms.ads.AdRequest.Builder().build());
    }

    @Override
    protected void onDestroy() {
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.destroy();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
