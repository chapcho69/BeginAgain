package com.olivearchi.goodroutine;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MemorizationActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TodoDbHelper dbHelper;
    private MemorizationAdapter adapter;
    private RecyclerView recyclerView;
    private TextToSpeech tts;
    private List<MemorizationItem> currentItems;
    private boolean isFavoriteFilterActive = false;

    private int currentPlayIndex = -1;
    private boolean isPlayingAll = false;
    private boolean isRandomMode = false;
    private List<Integer> shuffleIndices;
    private AlertDialog autoPlayDialog;
    private int repeatCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorization);

        setSupportActionBar(findViewById(R.id.toolbar_memo));

        dbHelper = new TodoDbHelper(this);
        recyclerView = findViewById(R.id.recycler_memorizations);

        FloatingActionButton fab = findViewById(R.id.fab_add_memo);
        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, MemorizationEditActivity.class));
        });

        MaterialButton btnReadAll = findViewById(R.id.btn_memo_read_all);
        btnReadAll.setOnClickListener(v -> togglePlaySequential());

        MaterialButton btnReadRandom = findViewById(R.id.btn_memo_read_random);
        btnReadRandom.setOnClickListener(v -> togglePlayRandom());

        tts = new TextToSpeech(this, this);

        loadMemos();
        initAds();
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
        if (currentItems == null || currentItems.isEmpty()) return;
        isPlayingAll = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (isRandomMode) {
            shuffleIndices = new ArrayList<>();
            for (int i = 0; i < currentItems.size(); i++) shuffleIndices.add(i);
            Collections.shuffle(shuffleIndices);
            currentPlayIndex = 0;
        } else {
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            currentPlayIndex = lm != null ? lm.findFirstVisibleItemPosition() : 0;
            if (currentPlayIndex < 0) currentPlayIndex = 0;
        }
        updateButtonStates();
        playNextItem();
    }

    private void stopPlayback() {
        isPlayingAll = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (tts != null) tts.stop();
        if (autoPlayDialog != null) autoPlayDialog.dismiss();
        runOnUiThread(this::updateButtonStates);
    }

    private void updateButtonStates() {
        MaterialButton btnAll = findViewById(R.id.btn_memo_read_all);
        MaterialButton btnRandom = findViewById(R.id.btn_memo_read_random);
        if (isPlayingAll) {
            if (isRandomMode) { btnAll.setText(R.string.btn_tts_all); btnRandom.setText(R.string.btn_stop); }
            else { btnAll.setText(R.string.btn_stop); btnRandom.setText(R.string.btn_tts_random); }
        } else { btnAll.setText(R.string.btn_tts_all); btnRandom.setText(R.string.btn_tts_random); }
    }

    private void playNextItem() {
        if (!isPlayingAll || currentItems == null) { stopPlayback(); return; }
        int idx;
        if (isRandomMode) {
            if (shuffleIndices == null || currentPlayIndex >= shuffleIndices.size()) { stopPlayback(); return; }
            idx = shuffleIndices.get(currentPlayIndex);
            currentPlayIndex++;
        } else {
            if (currentPlayIndex >= currentItems.size()) { stopPlayback(); return; }
            idx = currentPlayIndex;
            currentPlayIndex++;
        }
        MemorizationItem item = currentItems.get(idx);
        recyclerView.scrollToPosition(idx);
        showAutoPlayOverlay(item);
    }

    private void showAutoPlayOverlay(MemorizationItem item) {
        if (autoPlayDialog != null) autoPlayDialog.dismiss();
        View view = getLayoutInflater().inflate(R.layout.activity_memorization_detail, null);
        ((TextView)view.findViewById(R.id.text_memo_detail_keyword)).setText(item.getKeyword());
        ((TextView)view.findViewById(R.id.text_memo_detail_title)).setText(item.getTitle());
        ((TextView)view.findViewById(R.id.text_memo_detail_content)).setText(item.getContent());
        view.findViewById(R.id.layout_memo_detail_buttons).setVisibility(View.GONE);
        autoPlayDialog = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                .setView(view).setCancelable(true).setOnCancelListener(dialog -> stopPlayback()).create();
        autoPlayDialog.show();
        repeatCount = 0;
        startItemSequence(item);
    }

    private void startItemSequence(MemorizationItem item) {
        int q = TextToSpeech.QUEUE_FLUSH;
        speakText(getString(R.string.label_title) + ": " + item.getTitle(), q, "Part_W");
        String finalId = "Final_" + repeatCount;
        speakText(getString(R.string.label_content) + ": " + item.getContent(), TextToSpeech.QUEUE_ADD, finalId);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.KOREAN);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String utteranceId) {}
                @Override
                public void onDone(String utteranceId) {
                    if (isPlayingAll && utteranceId.startsWith("Final_")) {
                        runOnUiThread(() -> {
                            if (autoPlayDialog != null) autoPlayDialog.dismiss();
                            playNextItem();
                        });
                    }
                }
                @Override public void onError(String utteranceId) {}
            });
        }
    }

    private void speakText(String text, int mode, String id) {
        if (tts != null) {
            float speed = getSharedPreferences("AppPrefs", MODE_PRIVATE).getFloat("ttsSpeed", 1.0f);
            int voiceType = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("ttsVoiceType", 0);
            tts.setSpeechRate(speed);
            boolean voiceSet = false;
            Set<Voice> voices = tts.getVoices();
            if (voices != null) {
                for (Voice v : voices) {
                    if (v.getLocale().getLanguage().equals("ko")) {
                        String name = v.getName().toLowerCase();
                        if (voiceType == 1) {
                            if (name.contains("male") || name.contains("-b")) { tts.setVoice(v); voiceSet = true; break; }
                        } else {
                            if (name.contains("female") || name.contains("-a")) { tts.setVoice(v); voiceSet = true; break; }
                        }
                    }
                }
            }
            if (!voiceSet) tts.setPitch(voiceType == 0 ? 1.0f : 0.75f);
            else tts.setPitch(1.0f);
            tts.speak(text, mode, null, id);
        }
    }

    private void loadMemos() {
        List<MemorizationItem> allItems = dbHelper.getAllMemorizations();
        currentItems = new ArrayList<>();
        if (isFavoriteFilterActive) {
            for (MemorizationItem m : allItems) if (m.isFavorite()) currentItems.add(m);
        } else {
            currentItems.addAll(allItems);
        }
        if (getSupportActionBar() != null) {
            String title = isFavoriteFilterActive ? getString(R.string.feature_memorization_favorite) : getString(R.string.feature_memorization);
            getSupportActionBar().setTitle(title + "(" + currentItems.size() + ")");
        }
        adapter = new MemorizationAdapter(currentItems, item -> {
            Intent intent = new Intent(this, MemorizationDetailActivity.class);
            intent.putExtra("memo_item", item);
            startActivity(intent);
        }, text -> speakText(text, TextToSpeech.QUEUE_FLUSH, "Single"), item -> {
            dbHelper.toggleMemorizationFavorite(item.getId(), item.isFavorite());
        });
        recyclerView.setAdapter(adapter);
    }

    @Override protected void onResume() { super.onResume(); if (!isPlayingAll) loadMemos(); }
    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.menu_list, menu); return true; }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_home) {
            stopPlayback();
            Intent intent = new Intent(this, SelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_filter_favorite) {
            isFavoriteFilterActive = !isFavoriteFilterActive;
            invalidateOptionsMenu();
            loadMemos();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        stopPlayback();
        finish();
        return true;
    }

    private void initAds() {
        MobileAds.initialize(this, status -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
