package com.olivearchi.goodroutine;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SelectionActivity extends AppCompatActivity {

    private TodoViewModel viewModel;
    private TextToSpeech tempTts;
    private final FrameLayout[] slots = new FrameLayout[19];
    private final String[] slotAssignments = new String[19]; // index 0 to 18
    private MediaPlayer clickSoundPlayer;
    private final Handler soundHandler = new Handler(Looper.getMainLooper());
    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;
    private View honeycombContainer;
    private final Map<String, Integer> featureColors = new HashMap<>();
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private static final int LONG_PRESS_TIME = 3000;

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> { if (uri != null) restoreFromUri(uri); }
    );

    private final ActivityResultLauncher<String[]> dbPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> { if (uri != null) restoreFromRawDb(uri); }
    );

    private final ActivityResultLauncher<String> saveFileLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/octet-stream"),
            uri -> { if (uri != null) saveToUri(uri); }
    );

    private final ActivityResultLauncher<String> saveDbLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/x-sqlite3"),
            uri -> { if (uri != null) exportRawDb(uri); }
    );

    private final ActivityResultLauncher<String[]> importEnglishLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> { if (uri != null) importWords(uri, true); }
    );

    private final ActivityResultLauncher<String[]> importJapaneseLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> { if (uri != null) importWords(uri, false); }
    );

    private final ActivityResultLauncher<String> exportEnglishLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/plain"),
            uri -> { if (uri != null) exportWords(uri, true); }
    );

    private final ActivityResultLauncher<String> exportJapaneseLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/plain"),
            uri -> { if (uri != null) exportWords(uri, false); }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        viewModel = new ViewModelProvider(this).get(TodoViewModel.class);
        setSupportActionBar(findViewById(R.id.toolbar_selection));

        for (int i = 0; i < 19; i++) {
            int resId = getResources().getIdentifier("slot" + (i + 1), "id", getPackageName());
            slots[i] = findViewById(resId);
            slots[i].setTag(i);
            slots[i].setOnDragListener(dragListener);
        }

        loadSlotAssignments();
        refreshHoneycomb();
        initAds();
        setupScatteredBackground();

        honeycombContainer = findViewById(R.id.honeycomb_grid);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@androidx.annotation.NonNull ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));
                honeycombContainer.setScaleX(scaleFactor);
                honeycombContainer.setScaleY(scaleFactor);
                return true;
            }
        });

        clickSoundPlayer = MediaPlayer.create(this, R.raw.mouse_click_01);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { showExitConfirmation(); }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void loadSlotAssignments() {
        TodoDbHelper db = new TodoDbHelper(this);
        List<FeatureItem> features = db.getAllFeatures();
        String[] requiredKeys = {"routine", "memo", "english", "reading", "today", "memorization", "search", "japanese", "settings", "secret", "dashboard"};
        
        for (int i = 0; i < 19; i++) slotAssignments[i] = null;
        featureColors.clear();

        if (features.isEmpty()) {
            // Initial/Default Setup
            slotAssignments[3] = "routine";
            slotAssignments[4] = "memo";
            slotAssignments[5] = "english";
            slotAssignments[8] = "reading";
            slotAssignments[9] = "search";
            slotAssignments[10] = "today";
            slotAssignments[11] = "japanese";
            slotAssignments[13] = "memorization";
            slotAssignments[15] = "settings";
            slotAssignments[17] = "secret";
            slotAssignments[18] = "dashboard";
            
            // Default color is Pastel Blue for all except search
            int defaultColor = ContextCompat.getColor(this, R.color.pastel_blue);
            for (String key : requiredKeys) {
                featureColors.put(key, key.equals("search") ? 0xFFB0BEC5 : defaultColor);
            }
            saveSlotAssignments();
        } else {
            Set<String> assignedKeys = new HashSet<>();
            for (FeatureItem f : features) {
                if (f.getPosition() >= 0 && f.getPosition() < 19) {
                    slotAssignments[f.getPosition()] = f.getFeatureId();
                    featureColors.put(f.getFeatureId(), f.getColor());
                    assignedKeys.add(f.getFeatureId());
                }
            }
            
            // Check for missing required keys
            for (String req : requiredKeys) {
                if (!assignedKeys.contains(req)) {
                    int emptySlot = findNextEmptySlot();
                    if (emptySlot != -1) {
                        slotAssignments[emptySlot] = req;
                        featureColors.put(req, req.equals("search") ? 0xFFB0BEC5 : ContextCompat.getColor(this, R.color.pastel_blue));
                    }
                }
            }
            saveSlotAssignments();
        }
    }

    private int findNextEmptySlot() {
        for (int i = 0; i < 19; i++) {
            if (slotAssignments[i] == null) return i;
        }
        return -1;
    }

    private void saveSlotAssignments() {
        TodoDbHelper db = new TodoDbHelper(this);
        int defaultBlue = ContextCompat.getColor(this, R.color.pastel_blue);
        for (int i = 0; i < 19; i++) {
            String featureId = slotAssignments[i];
            if (featureId != null) {
                Integer colorObj = featureColors.get(featureId);
                int color = (colorObj != null) ? colorObj : defaultBlue;
                db.saveFeature(new FeatureItem(featureId, i, getFeatureTitle(featureId), "", color));
            }
        }
    }

    private String getFeatureTitle(String id) {
        switch (id) {
            case "routine": return "습관";
            case "reading": return "독서노트";
            case "memo": return "메모";
            case "memorization": return "암기장";
            case "today": return "할일들";
            case "english": return "영단어";
            case "japanese": return "일본어";
            case "settings": return "설정";
            case "search": return "찾기";
            case "secret": return "비밀노트";
            case "dashboard": return "통계";
            default: return "";
        }
    }

    private void resetFeaturePositions() {
        TodoDbHelper db = new TodoDbHelper(this);
        db.resetFeaturePositions();
        
        for (int i = 0; i < 19; i++) slotAssignments[i] = null;
        String[] keys = {"routine", "memo", "english", "reading", "today", "memorization", "search", "japanese", "settings", "secret", "dashboard"};
        System.arraycopy(keys, 0, slotAssignments, 0, Math.min(keys.length, 19));
        saveSlotAssignments();
        refreshHoneycomb();
        Toast.makeText(this, "위치가 초기화되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void refreshHoneycomb() {
        TodoDbHelper db = new TodoDbHelper(this);
        for (int i = 0; i < 19; i++) {
            // Remove existing buttons but keep background ImageView
            for (int j = 0; j < slots[i].getChildCount(); j++) {
                View child = slots[i].getChildAt(j);
                if (child instanceof MaterialButton) {
                    slots[i].removeView(child);
                    j--;
                }
            }

            ImageView bg = (ImageView) slots[i].getChildAt(0);
            String type = slotAssignments[i];

            if (type != null) {
                // Metal background for all feature buttons
                bg.setImageResource(R.drawable.bg_hexagon_metal);
                Integer tintColorObj = featureColors.get(type);
                int tintColor = (tintColorObj != null) ? tintColorObj : 0;
                if (tintColor != 0) {
                    bg.setImageTintList(android.content.res.ColorStateList.valueOf(tintColor));
                    bg.setImageTintMode(android.graphics.PorterDuff.Mode.MULTIPLY);
                } else {
                    bg.setImageTintList(null);
                }
                
                MaterialButton btn = new MaterialButton(this);
                btn.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                btn.setAllCaps(false);
                btn.setTypeface(null, android.graphics.Typeface.BOLD);
                btn.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                btn.setRippleColorResource(R.color.white);
                btn.setTextColor(0xFF000000);
                btn.setIconTint(android.content.res.ColorStateList.valueOf(0xFF000000));
                btn.setIconGravity(MaterialButton.ICON_GRAVITY_TOP);
                btn.setIconSize((int)(32 * getResources().getDisplayMetrics().density));
                btn.setIconPadding((int)(1 * getResources().getDisplayMetrics().density));
                btn.setPadding(0, (int)(12 * getResources().getDisplayMetrics().density), 0, 0);
                btn.setLineSpacing(0f, 0.9f);
                btn.setTag(i);

                String label = "";
                int iconRes = 0;
                View.OnClickListener clickListener = null;

                switch (type) {
                    case "routine":
                        label = "습관\n(" + db.getAllTodos().size() + ")";
                        iconRes = R.drawable.ic_bicycle;
                        clickListener = v -> startActivity(new Intent(this, MainActivity.class));
                        break;
                    case "reading":
                        label = "독서노트\n(" + db.getAllReadingNotes().size() + ")";
                        iconRes = R.drawable.ic_open_book;
                        clickListener = v -> startActivity(new Intent(this, ReadingNoteActivity.class));
                        break;
                    case "memo":
                        label = "메모\n(" + db.getAllMemos().size() + ")";
                        iconRes = R.drawable.ic_note;
                        clickListener = v -> startActivity(new Intent(this, MemoActivity.class));
                        break;
                    case "memorization":
                        label = "암기장\n(" + db.getAllMemorizations().size() + ")";
                        iconRes = R.drawable.ic_edit;
                        clickListener = v -> startActivity(new Intent(this, MemorizationActivity.class));
                        break;
                    case "today":
                        label = "할일들\n(" + db.getAllTodayTasks().size() + ")";
                        iconRes = R.drawable.ic_list;
                        clickListener = v -> startActivity(new Intent(this, TodayTaskListActivity.class));
                        break;
                    case "english":
                        label = "영단어\n(" + db.getAllEnglishWords().size() + ")";
                        iconRes = R.drawable.ic_headset;
                        clickListener = v -> startActivity(new Intent(this, EnglishWordActivity.class));
                        break;
                    case "japanese":
                        label = "일본어\n(" + db.getAllJapaneseWords().size() + ")";
                        iconRes = R.drawable.ic_headset;
                        clickListener = v -> startActivity(new Intent(this, JapaneseWordActivity.class));
                        break;
                    case "secret":
                        label = "비밀노트\n(" + db.getAllSecretNotes().size() + ")";
                        iconRes = R.drawable.ic_lock;
                        clickListener = v -> startActivity(new Intent(this, SecretNoteActivity.class));
                        break;
                    case "dashboard":
                        label = "통계\n(대시보드)";
                        iconRes = R.drawable.ic_clover;
                        clickListener = v -> startActivity(new Intent(this, DashboardActivity.class));
                        break;
                    case "settings":
                        label = "설정";
                        iconRes = R.drawable.ic_settings;
                        clickListener = v -> showSettingsDialog();
                        break;
                    case "search":
                        label = "찾기";
                        iconRes = android.R.drawable.ic_menu_search;
                        clickListener = v -> startActivity(new Intent(this, SearchActivity.class));
                        break;
                }

                btn.setText(label);
                btn.setIcon(ContextCompat.getDrawable(this, iconRes));
                
                final View.OnClickListener originalListener = clickListener;
                final String featureId = type;

                btn.setOnTouchListener(new View.OnTouchListener() {
                    private float startX, startY;
                    private boolean dragStarted = false;
                    private boolean colorPickerShown = false;
                    private final Runnable colorPickerRunnable = () -> {
                        if (!dragStarted) {
                            colorPickerShown = true;
                            playClickSound();
                            showColorPickerDialog(featureId);
                        }
                    };

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startX = event.getRawX();
                                startY = event.getRawY();
                                dragStarted = false;
                                colorPickerShown = false;
                                longPressHandler.postDelayed(colorPickerRunnable, LONG_PRESS_TIME);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (!colorPickerShown && !dragStarted) {
                                    float diffX = Math.abs(event.getRawX() - startX);
                                    float diffY = Math.abs(event.getRawY() - startY);
                                    if (diffX > 10 || diffY > 10) {
                                        dragStarted = true;
                                        longPressHandler.removeCallbacks(colorPickerRunnable);
                                        // Start Drag manually
                                        ClipData data = ClipData.newPlainText("slot_index", String.valueOf(v.getTag()));
                                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                                        v.startDragAndDrop(data, shadowBuilder, v, 0);
                                    }
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                longPressHandler.removeCallbacks(colorPickerRunnable);
                                if (!dragStarted && !colorPickerShown && event.getAction() == MotionEvent.ACTION_UP) {
                                    v.performClick();
                                }
                                break;
                        }
                        return true; // Consume touch to handle all phases
                    }
                });

                btn.setOnClickListener(v -> {
                    playClickSound();
                    if (originalListener != null) originalListener.onClick(v);
                });

                // No standard onLongClick to avoid conflict
                btn.setOnLongClickListener(null);

                slots[i].addView(btn);
            } else {
                bg.setImageResource(R.drawable.bg_hexagon_empty);
                bg.setImageTintList(null); // Remove tint to show the dotted line color correctly
            }
        }
    }

    private final View.OnDragListener dragListener = (v, event) -> {
        int action = event.getAction();
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
            case DragEvent.ACTION_DROP:
                int fromIndex = Integer.parseInt(event.getClipData().getItemAt(0).getText().toString());
                int toIndex = (int) v.getTag();
                
                if (fromIndex != toIndex) {
                    String temp = slotAssignments[toIndex];
                    slotAssignments[toIndex] = slotAssignments[fromIndex];
                    slotAssignments[fromIndex] = temp;
                    saveSlotAssignments();
                    refreshHoneycomb();
                }
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                return true;
            default:
                break;
        }
        return true;
    };

    private void setupScatteredBackground() {
        FrameLayout container = findViewById(R.id.background_container);
        if (container == null) return;
        Random random = new Random();
        int count = 7;
        for (int i = 0; i < count; i++) {
            TextView tv = new TextView(this);
            tv.setText("Begin Again");
            tv.setTextSize(20 + random.nextInt(60));
            tv.setTypeface(null, android.graphics.Typeface.BOLD_ITALIC);
            tv.setTextColor(i == count - 1 ? 0x66FF0000 : 0xFFEEEEEE);
            tv.setRotation(-45 + random.nextInt(90));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
            params.gravity = Gravity.TOP | Gravity.START;
            params.leftMargin = random.nextInt(Math.max(1, getResources().getDisplayMetrics().widthPixels - 200));
            params.topMargin = random.nextInt(Math.max(1, getResources().getDisplayMetrics().heightPixels - 300));
            container.addView(tv, params);
        }
    }

    private void playClickSound() {
        if (clickSoundPlayer != null) {
            clickSoundPlayer.seekTo(0);
            clickSoundPlayer.start();
            // Stop sound after 1 second to avoid long tail
            soundHandler.postDelayed(() -> {
                if (clickSoundPlayer != null && clickSoundPlayer.isPlaying()) {
                    clickSoundPlayer.pause();
                }
            }, 1000);
        }
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this).setTitle("앱 종료").setMessage("앱을 종료하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> finish())
                .setNegativeButton("아니오", null)
                .setOnCancelListener(dialog -> finish()) // Exit even if back button is pressed to cancel dialog
                .show();
    }

    private void updateAppTitle() {
        String customName = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("appName", "Begin Again");
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(customName);
        refreshHoneycomb();
    }

    @Override protected void onResume() { super.onResume(); updateAppTitle(); }
    @Override protected void onPause() { super.onPause(); if (viewModel != null) viewModel.backupData(); }

    private void initAds() {
        MobileAds.initialize(this, status -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.loadAd(new AdRequest.Builder().build());
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) { getMenuInflater().inflate(R.menu.menu_main, menu); return true; }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) { showSettingsDialog(); return true; }
        if (id == R.id.action_backup) { showBackupOptions(); return true; }
        if (id == R.id.action_restore) { showRestoreDialog(); return true; }
        if (id == R.id.action_word_mgmt) { showWordManagementOptions(); return true; }
        if (id == R.id.action_rename_app) { showRenameAppDialog(); return true; }
        if (id == R.id.action_tts_speed) { showTtsSpeedDialog(); return true; }
        if (id == R.id.action_tts_voice) { showTtsVoiceDialog(); return true; }
        if (id == R.id.action_about) { showAboutDialog(); return true; }
        return super.onOptionsItemSelected(item);
    }


    private void confirmClearWords(boolean isEnglish) {
        String title = isEnglish ? "영단어장 초기화" : "일본어장 초기화";
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("모든 단어 데이터가 삭제됩니다. 초기화 후에는 복구할 수 없습니다. 계속하시겠습니까?")
                .setPositiveButton("초기화", (dialog, which) -> {
                    TodoDbHelper db = new TodoDbHelper(this);
                    if (isEnglish) db.clearAllEnglishWords();
                    else db.clearAllJapaneseWords();
                    Toast.makeText(this, "초기화되었습니다.", Toast.LENGTH_SHORT).show();
                    refreshHoneycomb();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void importWords(Uri uri, boolean isEnglish) {
        TodoDbHelper db = new TodoDbHelper(this);
        int count = 0;
        try (InputStream is = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (isEnglish && parts.length >= 4) {
                    db.addEnglishWord(new EnglishWordItem(parts[0].trim(), parts[1].trim(), parts[2].trim(), "", Integer.parseInt(parts[3].trim())));
                    count++;
                } else if (!isEnglish && parts.length >= 5) {
                    db.addJapaneseWord(new JapaneseWordItem(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), Integer.parseInt(parts[4].trim())));
                    count++;
                }
            }
            Toast.makeText(this, count + "개의 단어를 가져왔습니다.", Toast.LENGTH_SHORT).show();
            refreshHoneycomb();
        } catch (Exception e) {
            Toast.makeText(this, "가져오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportWords(Uri uri, boolean isEnglish) {
        TodoDbHelper db = new TodoDbHelper(this);
        try (OutputStream os = getContentResolver().openOutputStream(uri);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            if (isEnglish) {
                for (EnglishWordItem item : db.getAllEnglishWords()) {
                    writer.write(item.getWord() + "|" + item.getMeaning() + "|" + item.getExample() + "|" + item.getLevel());
                    writer.newLine();
                }
            } else {
                for (JapaneseWordItem item : db.getAllJapaneseWords()) {
                    writer.write(item.getWord() + "|" + item.getReading() + "|" + item.getMeaning() + "|" + item.getExample() + "|" + item.getLevel());
                    writer.newLine();
                }
            }
            Toast.makeText(this, "내보내기 완료", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "내보내기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSettingsDialog() {
        String[] categories = {"데이터 관리", "단어장 관리", "기타 설정", "About"};
        new AlertDialog.Builder(this)
                .setTitle("설정")
                .setItems(categories, (dialog, which) -> {
                    switch (which) {
                        case 0: showDataManagementOptions(); break;
                        case 1: showWordManagementOptions(); break;
                        case 2: showOtherSettingsOptions(); break;
                        case 3: showAboutDialog(); break;
                    }
                })
                .show();
    }

    private void showDataManagementOptions() {
        String[] options = {"데이터 보관 (.dat)", "데이터 복구 (.dat)", "SQLite DB 파일로 내보내기", "SQLite DB 파일에서 복구"};
        new AlertDialog.Builder(this)
                .setTitle("데이터 관리")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showBackupOptions();
                    else if (which == 1) showRestoreDialog();
                    else if (which == 2) saveDbLauncher.launch("goodroutine_raw.db");
                    else confirmRawDbRestore();
                }).show();
    }

    private void confirmRawDbRestore() {
        new AlertDialog.Builder(this)
                .setTitle("DB 파일 복구")
                .setMessage("주의: 복구 시 현재 앱의 모든 데이터가 선택한 DB 파일로 대체되고 기존 데이터는 삭제됩니다. 계속하시겠습니까?")
                .setPositiveButton("파일 선택", (d, w) -> dbPickerLauncher.launch(new String[]{"application/x-sqlite3", "application/octet-stream", "*/*"}))
                .setNegativeButton("취소", null)
                .show();
    }

    private void restoreFromRawDb(Uri uri) {
        try {
            // Close existing DB connection first
            TodoDbHelper helper = new TodoDbHelper(this);
            helper.getWritableDatabase().close();
            
            File dbFile = getDatabasePath("todos.db");
            try (InputStream is = getContentResolver().openInputStream(uri);
                 OutputStream os = new FileOutputStream(dbFile)) {
                if (is == null || os == null) throw new java.io.IOException("Stream is null");
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
            
            // Re-initialize and update schema if needed
            SQLiteDatabase newDb = helper.getWritableDatabase();
            helper.onUpgrade(newDb, newDb.getVersion(), TodoDbHelper.DATABASE_VERSION);
            
            Toast.makeText(this, "DB 복구 완료. 앱 설정을 갱신합니다.", Toast.LENGTH_SHORT).show();
            loadSlotAssignments();
            refreshHoneycomb();
        } catch (Exception e) {
            Toast.makeText(this, "복구 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("SelectionActivity", "Raw DB restore failed", e);
        }
    }

    private void exportRawDb(Uri uri) {
        File dbFile = getDatabasePath("todos.db");
        try (InputStream is = new FileInputStream(dbFile);
             OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os == null) throw new java.io.IOException("OutputStream is null");
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            Toast.makeText(this, "DB 파일 내보내기 완료", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "내보내기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showWordManagementOptions() {
        String[] options = {
                "영단어 가져오기", "일본어 가져오기",
                "영어 단어 정렬 (중복 제거)", "일어 단어 정렬 (중복 제거)",
                "영단어 내보내기", "일어 내보내기",
                "영단어장 초기화", "일어장 초기화"
        };
        new AlertDialog.Builder(this).setTitle("단어장 관리").setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: importEnglishLauncher.launch(new String[]{"text/plain", "*/*"}); break;
                case 1: importJapaneseLauncher.launch(new String[]{"text/plain", "*/*"}); break;
                case 2: deduplicateWords(true); break;
                case 3: deduplicateWords(false); break;
                case 4: exportEnglishLauncher.launch("english_words_export.txt"); break;
                case 5: exportJapaneseLauncher.launch("japanese_words_export.txt"); break;
                case 6: confirmClearWords(true); break;
                case 7: confirmClearWords(false); break;
            }
        }).show();
    }

    private void showColorPickerDialog(String featureId) {
        String[] colorNames = {"기본 (파스텔 블루)", "부드러운 레드", "숲의 초록", "밝은 노란색", "깊은 바다색", "보라색", "오렌지"};
        int[] colors = {
                ContextCompat.getColor(this, R.color.pastel_blue),
                0xFFFFCDD2, 0xFFC8E6C9, 0xFFFFF9C4, 0xFFBBDEFB, 0xFFE1BEE7, 0xFFFFE0B2
        };

        new AlertDialog.Builder(this)
                .setTitle("배경 색상 변경")
                .setItems(colorNames, (dialog, which) -> {
                    int selectedColor = colors[which];
                    featureColors.put(featureId, selectedColor);
                    TodoDbHelper db = new TodoDbHelper(this);
                    db.updateFeatureColor(featureId, selectedColor);
                    refreshHoneycomb();
                    Toast.makeText(this, "색상이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showOtherSettingsOptions() {
        String[] options = {"App 명칭 수정", "TTS 속도 설정", "TTS 목소리 설정", "위치 초기화"};
        new AlertDialog.Builder(this)
                .setTitle("기타 설정")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: showRenameAppDialog(); break;
                        case 1: showTtsSpeedDialog(); break;
                        case 2: showTtsVoiceDialog(); break;
                        case 3: confirmResetPositions(); break;
                    }
                }).show();
    }

    private void confirmResetPositions() {
        new AlertDialog.Builder(this)
                .setTitle("위치 초기화")
                .setMessage("메인 화면의 모든 버튼 위치가 초기 상태로 돌아갑니다. 계속하시겠습니까?")
                .setPositiveButton("초기화", (dialog, which) -> resetFeaturePositions())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deduplicateWords(boolean isEnglish) {
        TodoDbHelper db = new TodoDbHelper(this);
        List<?> allWords = isEnglish ? db.getAllEnglishWords() : db.getAllJapaneseWords();
        if (allWords.isEmpty()) {
            Toast.makeText(this, "단어가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.ProgressDialog progress = new android.app.ProgressDialog(this);
        progress.setTitle(isEnglish ? "영어 단어 정렬" : "일어 단어 정렬");
        progress.setMessage("중복을 확인하는 중...");
        progress.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setMax(allWords.size());
        progress.setCancelable(false);
        progress.show();

        new Thread(() -> {
            int total = allWords.size();
            Map<String, Object> uniqueMap = new HashMap<>();
            
            for (int i = 0; i < total; i++) {
                final int current = i + 1;
                Object item = allWords.get(i);
                String key = isEnglish ? ((EnglishWordItem)item).getWord().toLowerCase() : 
                                       (((JapaneseWordItem)item).getWord() + "|" + ((JapaneseWordItem)item).getReading()).toLowerCase();
                String meaning = isEnglish ? ((EnglishWordItem)item).getMeaning() : ((JapaneseWordItem)item).getMeaning();

                if (!uniqueMap.containsKey(key)) {
                    uniqueMap.put(key, item);
                } else {
                    Object existing = uniqueMap.get(key);
                    String existingMeaning = isEnglish ? ((EnglishWordItem)existing).getMeaning() : ((JapaneseWordItem)existing).getMeaning();
                    if (meaning.length() > existingMeaning.length()) {
                        uniqueMap.put(key, item);
                    }
                }
                
                runOnUiThread(() -> {
                    progress.setProgress(current);
                    progress.setMessage("진행 중: " + current + " / " + total);
                });
            }

            runOnUiThread(() -> progress.setMessage("저장 중..."));

            if (isEnglish) {
                db.clearAllEnglishWords();
                for (Object o : uniqueMap.values()) db.addEnglishWord((EnglishWordItem) o);
            } else {
                db.clearAllJapaneseWords();
                for (Object o : uniqueMap.values()) db.addJapaneseWord((JapaneseWordItem) o);
            }

            runOnUiThread(() -> {
                progress.dismiss();
                Toast.makeText(this, "정렬 및 중복 제거 완료 (" + (total - uniqueMap.size()) + "개 제거됨)", Toast.LENGTH_LONG).show();
                refreshHoneycomb();
            });
        }).start();
    }

    private void showTtsSpeedDialog() {
        final float[] speed = {getSharedPreferences("AppPrefs", MODE_PRIVATE).getFloat("ttsSpeed", 1.0f)};
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);

        Button btnDec = new Button(this);
        btnDec.setText("-");
        
        TextView tvSpeed = new TextView(this);
        tvSpeed.setText(String.format(Locale.getDefault(), "%.1f", speed[0]));
        tvSpeed.setTextSize(24);
        tvSpeed.setPadding(40, 0, 40, 0);
        tvSpeed.setTextColor(0xFF000000);

        Button btnInc = new Button(this);
        btnInc.setText("+");

        btnDec.setOnClickListener(v -> {
            if (speed[0] > 0.15f) {
                speed[0] -= 0.1f;
                tvSpeed.setText(String.format(Locale.getDefault(), "%.1f", speed[0]));
            }
        });

        btnInc.setOnClickListener(v -> {
            if (speed[0] < 1.95f) {
                speed[0] += 0.1f;
                tvSpeed.setText(String.format(Locale.getDefault(), "%.1f", speed[0]));
            }
        });

        layout.addView(btnDec);
        layout.addView(tvSpeed);
        layout.addView(btnInc);

        new AlertDialog.Builder(this)
                .setTitle("TTS 속도 설정 (0.1 ~ 2.0)")
                .setView(layout)
                .setPositiveButton("저장", (dialog, which) -> {
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putFloat("ttsSpeed", speed[0]).apply();
                    Toast.makeText(this, "속도가 " + String.format(Locale.getDefault(), "%.1f", speed[0]) + "배속으로 설정되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showTtsVoiceDialog() {
        String[] options = {"여성 목소리", "남성 목소리"};
        int currentType = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("ttsVoiceType", 0);
        new AlertDialog.Builder(this).setTitle("TTS 목소리 성별 선택").setSingleChoiceItems(options, currentType, (dialog, which) -> {
            getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putInt("ttsVoiceType", which).putString("ttsVoiceName", "").apply();
            dialog.dismiss();
            Toast.makeText(this, (which == 0 ? "여성" : "남성") + " 목소리로 설정되었습니다.", Toast.LENGTH_SHORT).show();
        }).setPositiveButton("목소리 상세 선택", (dialog, which) -> showAdvancedTtsVoiceDialog()).setNegativeButton("취소", null).show();
    }

    private void showAdvancedTtsVoiceDialog() {
        if (tempTts != null) tempTts.shutdown();
        tempTts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Set<Voice> allVoices = tempTts.getVoices();
                List<Voice> koVoices = new ArrayList<>();
                if (allVoices != null) for (Voice v : allVoices) if (v.getLocale().getLanguage().equals("ko")) koVoices.add(v);
                if (koVoices.isEmpty()) { runOnUiThread(() -> Toast.makeText(this, "한국어 목소리가 없습니다.", Toast.LENGTH_SHORT).show()); return; }
                String[] voiceNames = new String[koVoices.size()];
                for (int i = 0; i < koVoices.size(); i++) voiceNames[i] = koVoices.get(i).getName();
                String currentVoiceName = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("ttsVoiceName", "");
                int initialChecked = -1;
                for (int i = 0; i < voiceNames.length; i++) if (voiceNames[i].equals(currentVoiceName)) initialChecked = i;
                final int checkedItem = initialChecked;
                runOnUiThread(() -> new AlertDialog.Builder(this).setTitle("TTS 목소리 상세 선택").setSingleChoiceItems(voiceNames, checkedItem, (dialog, which) -> {
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("ttsVoiceName", voiceNames[which]).apply();
                    dialog.dismiss();
                    Toast.makeText(this, "목소리가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                }).setPositiveButton("시스템 설정", (dialog, which) -> {
                    try { startActivity(new Intent("com.android.settings.TTS_SETTINGS")); } catch (Exception e) { Toast.makeText(this, "설정 화면을 열 수 없습니다.", Toast.LENGTH_SHORT).show(); }
                }).setNegativeButton("취소", null).show());
            }
        });
    }

    @Override protected void onDestroy() { 
        soundHandler.removeCallbacksAndMessages(null);
        if (clickSoundPlayer != null) {
            clickSoundPlayer.release();
            clickSoundPlayer = null;
        }
        if (tempTts != null) tempTts.shutdown(); 
        super.onDestroy(); 
    }
    private void showBackupOptions() {
        String[] options = {"공유하기", "다른 위치에 저장하기"};
        new AlertDialog.Builder(this).setTitle("데이터 보관 방식 선택").setItems(options, (dialog, which) -> {
            if (which == 0) shareBackupFile();
            else saveFileLauncher.launch("todos_backup_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".dat");
        }).show();
    }

    private void shareBackupFile() {
        viewModel.backupData();
        File backupFile = new File(getCacheDir(), "todos_backup.dat");
        if (!backupFile.exists()) { Toast.makeText(this, "백업 파일 생성 실패", Toast.LENGTH_SHORT).show(); return; }
        try {
            Uri contentUri = FileProvider.getUriForFile(this, "com.olivearchi.goodroutine.fileprovider", backupFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream").putExtra(Intent.EXTRA_SUBJECT, "Begin Again - 데이터 백업").putExtra(Intent.EXTRA_STREAM, contentUri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "백업 파일 보내기"));
        } catch (Exception e) { Toast.makeText(this, "백업 공유 실패", Toast.LENGTH_SHORT).show(); }
    }

    private void saveToUri(Uri uri) {
        viewModel.backupData();
        File internalBackup = new File(getCacheDir(), "todos_backup.dat");
        try (InputStream is = new FileInputStream(internalBackup); OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os == null) throw new java.io.IOException("OutputStream is null");
            byte[] buffer = new byte[1024]; int length;
            while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length);
            Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
    }

    private void showRestoreDialog() {
        new AlertDialog.Builder(this).setTitle("데이터 복구").setMessage("복구 시 현재 데이터가 모두 삭제되고 백업 데이터로 대체됩니다. 계속할까요?")
                .setPositiveButton("파일 선택", (dialog, which) -> filePickerLauncher.launch(new String[]{"application/octet-stream", "*/*"})).setNegativeButton("취소", null).show();
    }

    private void restoreFromUri(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             ObjectInputStream ois = new ObjectInputStream(is) {
                 @Override
                 protected ObjectStreamClass readClassDescriptor() throws java.io.IOException, ClassNotFoundException {
                     ObjectStreamClass desc = super.readClassDescriptor();
                     if (desc.getName().startsWith("com.example.goodroutine")) {
                         String newName = desc.getName().replace("com.example.goodroutine", "com.olivearchi.goodroutine");
                         return ObjectStreamClass.lookup(Class.forName(newName));
                     }
                     return desc;
                 }
             }) {
            Object data = ois.readObject();
            if (data != null) {
                viewModel.restoreData(data);
                Toast.makeText(this, "데이터 복구 성공", Toast.LENGTH_SHORT).show();
                refreshHoneycomb();
            }
        } catch (Exception e) {
            Log.e("SelectionActivity", "Restore failed", e);
            Toast.makeText(this, "복구 실패: 올바른 백업 파일이 아닙니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRenameAppDialog() {
        EditText input = new EditText(this);
        input.setText(getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("appName", "Begin Again"));
        new AlertDialog.Builder(this).setTitle("App 명칭 수정").setView(input).setPositiveButton("저장", (dialog, which) -> {
            String newName = input.getText().toString();
            if (!newName.isEmpty()) { getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("appName", newName).apply(); updateAppTitle(); }
        }).setNegativeButton("취소", null).show();
    }

    private void showAboutDialog() {
        String version = getString(R.string.app_version);
        String buildDate = getString(R.string.build_date);
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("버전: " + version + "\n마지막 빌드: " + buildDate + "\n제작자 정보: Routine Maker\n문의: managedswsvc@gmail.com")
                .setPositiveButton("확인", null)
                .setNeutralButton("제작의도", (dialog, which) -> showIntentionDialog())
                .show();
    }

    private void showIntentionDialog() {
        String content = "제작 의도\n" +
                "- 책에서 발견한 아름다운 구절을 수집하기 위함\n" +
                "  자주 보고 듣고 싶을때 도움 되는 App을 만들고자 함.\n" +
                "+ \"좋은 사람이 되려면, 좋은 루틴을 가져야 한다.\"는 말에 감명 받아, 루틴 수행 이력 추가.\n" +
                "+ 시간 관리 습관을 키우기 위해 \"해야 할 일들\" 기능 추가.\n" +
                "+ \"뇌 용량을 키우는 용도\"로 암기를 위한 항목 기능 추가.(메모장,영단어,일단어)\n" +
                "+ 검색 기능 추가.";
        
        new AlertDialog.Builder(this)
                .setTitle("제작 의도")
                .setMessage(content)
                .setPositiveButton("확인", null)
                .show();
    }
}
