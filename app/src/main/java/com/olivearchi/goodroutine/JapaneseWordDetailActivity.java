package com.olivearchi.goodroutine;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.speech.tts.Voice;

public class JapaneseWordDetailActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private JapaneseWordItem item;
    private List<JapaneseWordItem> wordList;
    private int currentIndex;
    private TextToSpeech tts;
    private boolean isAutoPlayMode = false;
    private int repeatCount = 0;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_japanese_word_detail);

        dbHelper = new TodoDbHelper(this);
        currentIndex = getIntent().getIntExtra("word_index", -1);
        isAutoPlayMode = getIntent().getBooleanExtra("auto_play", false);
        wordList = WordDataHolder.getJapaneseWords();

        if (wordList == null || currentIndex < 0 || currentIndex >= wordList.size()) {
            finish(); return;
        }

        setSupportActionBar(findViewById(R.id.toolbar_japanese_detail));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        updateUI();

        findViewById(R.id.btn_japanese_prev).setOnClickListener(v -> navigate(-1));
        findViewById(R.id.btn_japanese_next).setOnClickListener(v -> navigate(1));
        findViewById(R.id.btn_japanese_tts).setOnClickListener(v -> {
            repeatCount = 1;
            startSequence();
        });
        findViewById(R.id.btn_japanese_delete).setOnClickListener(v -> confirmDelete());

        tts = new TextToSpeech(this, this);
        initAds();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_delete)
                .setMessage(R.string.msg_confirm_delete)
                .setPositiveButton(R.string.label_yes, (dialog, which) -> {
                    dbHelper.deleteJapaneseWord(item.getId());
                    Toast.makeText(this, R.string.msg_delete_success, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.label_no, null)
                .show();
    }

    private void updateUI() {
        item = wordList.get(currentIndex);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.feature_japanese) + " (" + (currentIndex + 1) + "/" + wordList.size() + ")");
        }
        ((TextView)findViewById(R.id.text_japanese_word)).setText(item.getWord());
        ((TextView)findViewById(R.id.text_japanese_reading)).setText(item.getReading());
        ((TextView)findViewById(R.id.text_japanese_meaning)).setText(item.getMeaning());
        ((TextView)findViewById(R.id.text_japanese_example)).setText(item.getExample());
    }

    private void navigate(int delta) {
        int newIndex = currentIndex + delta;
        if (newIndex >= 0 && newIndex < wordList.size()) {
            currentIndex = newIndex;
            if (tts != null) tts.stop();
            updateUI();
        } else {
            Toast.makeText(this, delta > 0 ? getString(R.string.action_home) : getString(R.string.action_home), Toast.LENGTH_SHORT).show();
            // Need specific strings for "last word" "first word"
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.JAPANESE);
            setupTtsListener();
            if (isAutoPlayMode) {
                repeatCount = 0;
                startSequence();
            }
        }
    }

    private void setupTtsListener() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {}
            @Override
            public void onDone(String utteranceId) {
                if (utteranceId.startsWith("Final_")) {
                    repeatCount++;
                    if (isAutoPlayMode && repeatCount < 2) {
                        runOnUiThread(() -> startSequence());
                    } else if (isAutoPlayMode) {
                        runOnUiThread(() -> {
                            setResult(RESULT_OK);
                            finish();
                        });
                    }
                }
            }
            @Override public void onError(String utteranceId) {}
        });
    }

    private void startSequence() {
        int queueMode = TextToSpeech.QUEUE_FLUSH;
        speakText(item.getWord(), Locale.JAPANESE, queueMode, "Part_W");
        speakText(item.getReading(), Locale.JAPANESE, TextToSpeech.QUEUE_ADD, "Part_R");
        speakText(item.getMeaning(), Locale.KOREAN, TextToSpeech.QUEUE_ADD, "Part_M");
        
        String finalId = "Final_" + repeatCount;
        if (item.getExample() != null && !item.getExample().isEmpty()) {
            speakText(item.getExample(), Locale.JAPANESE, TextToSpeech.QUEUE_ADD, finalId);
        } else {
            speakText(item.getMeaning(), Locale.KOREAN, TextToSpeech.QUEUE_ADD, finalId);
        }
    }

    private void speakText(String text, Locale locale, int queueMode, String utteranceId) {
        if (tts != null) {
            float speed = getSharedPreferences("AppPrefs", MODE_PRIVATE).getFloat("ttsSpeed", 1.0f);
            int voiceType = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("ttsVoiceType", 0);
            tts.setSpeechRate(speed);
            tts.setLanguage(locale);
            boolean voiceSet = false;
            Set<Voice> voices = tts.getVoices();
            if (voices != null) {
                for (Voice v : voices) {
                    if (v.getLocale().getLanguage().equals(locale.getLanguage())) {
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
            tts.speak(text, queueMode, null, utteranceId);
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

    private void initAds() {
        MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    @Override
    protected void onPause() {
        if (tts != null) tts.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
