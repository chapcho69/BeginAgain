package com.olivearchi.goodroutine;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ReadingNoteDetailActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private ReadingNoteItem item;
    private TextToSpeech tts;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_note_detail);

        dbHelper = new TodoDbHelper(this);
        item = (ReadingNoteItem) getIntent().getSerializableExtra("note_item");

        if (item == null) {
            finish();
            return;
        }

        setSupportActionBar(findViewById(R.id.toolbar_detail));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.feature_reading);
        }

        ((TextView)findViewById(R.id.text_detail_book_title)).setText(SearchHighlightUtils.getHighlightedText(getString(R.string.label_book_title) + ": " + item.getBookTitle()));
        ((TextView)findViewById(R.id.text_detail_content)).setText(SearchHighlightUtils.getHighlightedText(item.getContent()));
        ((TextView)findViewById(R.id.text_detail_remarks)).setText(SearchHighlightUtils.getHighlightedText(getString(R.string.label_remarks) + ": " + item.getRemarks()));
        ((TextView)findViewById(R.id.text_detail_date)).setText(getString(R.string.label_last_modified) + ": " + item.getModifiedDateTime());

        MaterialButton btnEdit = findViewById(R.id.btn_detail_edit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReadingNoteEditActivity.class);
            intent.putExtra("note_item", item);
            startActivity(intent);
            finish();
        });

        MaterialButton btnTts = findViewById(R.id.btn_detail_tts);
        btnTts.setOnClickListener(v -> speakText(item.getContent()));

        MaterialButton btnShare = findViewById(R.id.btn_detail_share);
        btnShare.setOnClickListener(v -> showShareOptions());

        tts = new TextToSpeech(this, this);
        initAds();
    }

    private void showShareOptions() {
        String[] options = {getString(R.string.btn_chooser_share) + " (Text)", getString(R.string.btn_chooser_share) + " (Image)"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.btn_share)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        shareContent(item.getBookTitle(), item.getContent());
                    } else {
                        shareAsImages();
                    }
                })
                .show();
    }

    private void shareAsImages() {
        List<String> chunks = splitContentByBytes(item.getContent(), 1500);
        ArrayList<Uri> imageUris = new ArrayList<>();
        File cachePath = new File(getCacheDir(), "images");
        cachePath.mkdirs();

        // Clean up previous share images
        File[] files = cachePath.listFiles();
        if (files != null) for (File f : files) f.delete();

        for (int i = 0; i < chunks.size(); i++) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.layout_reading_note_card, null);
            TextView tvTitle = cardView.findViewById(R.id.card_book_title);
            TextView tvContent = cardView.findViewById(R.id.card_content);
            TextView tvDate = cardView.findViewById(R.id.card_date);

            String displayTitle = item.getBookTitle();
            if (chunks.size() > 1) {
                displayTitle += " (" + (i + 1) + "/" + chunks.size() + ")";
            }
            tvTitle.setText(displayTitle);
            tvContent.setText(chunks.get(i));
            tvDate.setText(item.getModifiedDateTime());

            cardView.measure(View.MeasureSpec.makeMeasureSpec(1200, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            cardView.layout(0, 0, cardView.getMeasuredWidth(), cardView.getMeasuredHeight());

            Bitmap bitmap = Bitmap.createBitmap(cardView.getMeasuredWidth(), cardView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            cardView.draw(canvas);

            try {
                File file = new File(cachePath, "reading_note_card_" + i + ".png");
                FileOutputStream stream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                
                Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                imageUris.add(contentUri);
            } catch (IOException e) {
                Log.e("ReadingNoteDetail", "Image creation failed at index " + i, e);
            }
        }

        if (!imageUris.isEmpty()) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.setType("image/png");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.btn_share)));
        } else {
            Toast.makeText(this, "이미지 생성 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private List<String> splitContentByBytes(String content, int maxBytes) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isEmpty()) return chunks;

        byte[] fullBytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (fullBytes.length <= maxBytes) {
            chunks.add(content);
            return chunks;
        }

        int currentStart = 0;
        while (currentStart < content.length()) {
            int currentEnd = currentStart;
            int currentBytes = 0;
            while (currentEnd < content.length()) {
                int charBytes = String.valueOf(content.charAt(currentEnd)).getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
                if (currentBytes + charBytes > maxBytes) break;
                currentBytes += charBytes;
                currentEnd++;
            }
            chunks.add(content.substring(currentStart, currentEnd));
            currentStart = currentEnd;
        }
        return chunks;
    }

    private void shareContent(String title, String content) {
        String shareText = getString(R.string.label_title) + ": " + title + "\n\n" + content + "\n\nReflection from BeginAgain";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, getString(R.string.btn_share)));
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

    @Override
    protected void onResume() {
        super.onResume();
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.resume();
    }

    @Override
    protected void onPause() {
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.destroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void initAds() {
        com.google.android.gms.ads.MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
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
