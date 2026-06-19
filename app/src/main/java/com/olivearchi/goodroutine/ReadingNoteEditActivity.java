package com.olivearchi.goodroutine;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import androidx.core.widget.NestedScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReadingNoteEditActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TodoDbHelper dbHelper;
    private ReadingNoteItem currentItem;
    private TextInputEditText editBookTitle, editContent, editRemarks;
    private TextInputLayout layoutBookTitle, layoutContent, layoutRemarks;
    private NestedScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_note_edit);

        dbHelper = new TodoDbHelper(this);
        
        layoutBookTitle = findViewById(R.id.input_book_title);
        layoutContent = findViewById(R.id.input_note_content);
        layoutRemarks = findViewById(R.id.input_note_remarks);
        
        editBookTitle = findViewById(R.id.edit_book_title);
        editContent = findViewById(R.id.edit_note_content);
        editRemarks = findViewById(R.id.edit_note_remarks);
        scrollView = findViewById(R.id.scroll_reading_edit);

        setupVerticalScroll(editContent);
        setupVerticalScroll(editRemarks);

        keepCursorCentered(editContent, scrollView);
        keepCursorCentered(editRemarks, scrollView);

        MaterialButton btnSave = findViewById(R.id.btn_save_note);
        MaterialButton btnAddNext = findViewById(R.id.btn_add_next);
        MaterialButton btnDelete = findViewById(R.id.btn_delete_note);

        currentItem = (ReadingNoteItem) getIntent().getSerializableExtra("note_item");

        android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        editContent.getLayoutParams().height = (int)(screenHeight * 0.5);
        editContent.requestLayout();

        if (currentItem != null && currentItem.getId() != 0) {
            editBookTitle.setText(currentItem.getBookTitle());
            editContent.setText(currentItem.getContent());
            editRemarks.setText(currentItem.getRemarks());
            btnDelete.setVisibility(View.VISIBLE);
        } else if (currentItem != null && currentItem.getId() == 0) {
            editBookTitle.setText(currentItem.getBookTitle());
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // Setup ByteCounter AFTER setting initial text to avoid filter blocking load
        setupByteCounter(editBookTitle, layoutBookTitle, 100, getString(R.string.label_word));
        setupByteCounter(editContent, layoutContent, 2000, getString(R.string.label_content));
        setupByteCounter(editRemarks, layoutRemarks, 1000, getString(R.string.label_remarks));

        btnSave.setOnClickListener(v -> {
            if (saveNote()) finish();
        });

        btnAddNext.setOnClickListener(v -> {
            if (saveNote()) {
                String title = (editBookTitle.getText() != null) ? editBookTitle.getText().toString() : "";
                Intent intent = new Intent(this, ReadingNoteEditActivity.class);
                ReadingNoteItem template = new ReadingNoteItem(title, "", "", "");
                intent.putExtra("note_item", template);
                startActivity(intent);
                finish();
            }
        });

        btnDelete.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("삭제 확인")
                    .setMessage("정말 삭제 할까요?")
                    .setPositiveButton("예", (dialog, which) -> {
                        dbHelper.deleteReadingNote(currentItem.getId());
                        Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });
        
        initAds();
    }

    private void setupByteCounter(EditText editText, TextInputLayout layout, int maxBytes, String baseHint) {
        editText.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                int destBytes = dest.toString().getBytes(StandardCharsets.UTF_8).length;
                int sourceBytes = source.subSequence(start, end).toString().getBytes(StandardCharsets.UTF_8).length;
                int replacedBytes = dest.subSequence(dstart, dend).toString().getBytes(StandardCharsets.UTF_8).length;
                
                if (destBytes + sourceBytes - replacedBytes > maxBytes) {
                    if (dest.length() == 0 && source.length() > 0) {
                        return source.subSequence(0, findMaxCharsForBytes(source, maxBytes));
                    }
                    return "";
                }
                return null;
            }
        }});

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateHintWithByteCount(layout, s.toString(), maxBytes, baseHint);
            }
        });
        updateHintWithByteCount(layout, editText.getText().toString(), maxBytes, baseHint);
    }

    private int findMaxCharsForBytes(CharSequence source, int maxBytes) {
        int bytes = 0;
        for (int i = 0; i < source.length(); i++) {
            bytes += String.valueOf(source.charAt(i)).getBytes(StandardCharsets.UTF_8).length;
            if (bytes > maxBytes) return i;
        }
        return source.length();
    }

    private void updateHintWithByteCount(TextInputLayout layout, String text, int maxBytes, String baseHint) {
        int bytes = text.getBytes(StandardCharsets.UTF_8).length;
        int percent = Math.min(100, (int)((bytes / (float)maxBytes) * 100));
        layout.setHint(String.format(java.util.Locale.getDefault(), getString(R.string.msg_byte_usage), baseHint, percent));
    }

    private void setupVerticalScroll(android.view.View view) {
        view.setOnTouchListener((v, event) -> {
            if (v.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & android.view.MotionEvent.ACTION_MASK) == android.view.MotionEvent.ACTION_SCROLL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
            }
            return false;
        });
    }

    private void keepCursorCentered(EditText editText, NestedScrollView scrollView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                editText.post(() -> {
                    Layout layout = editText.getLayout();
                    if (layout != null) {
                        int pos = editText.getSelectionStart();
                        if (pos < 0) return;
                        int line = layout.getLineForOffset(pos);
                        int lineTop = layout.getLineTop(line);
                        int lineBottom = layout.getLineBottom(line);

                        // Calculate cursor position relative to the ScrollView's child (the LinearLayout)
                        int[] viewLocation = new int[2];
                        int[] scrollLocation = new int[2];
                        editText.getLocationInWindow(viewLocation);
                        scrollView.getLocationInWindow(scrollLocation);

                        int cursorYInWindow = viewLocation[1] + lineTop;
                        int scrollYInWindow = scrollLocation[1];
                        int cursorYInScroll = cursorYInWindow - scrollYInWindow + scrollView.getScrollY();

                        int scrollHeight = scrollView.getHeight();
                        if (scrollHeight > 0) {
                            // Aim to keep cursor at 30% from the top of the visible area
                            int targetScrollY = cursorYInScroll - (int)(scrollHeight * 0.3);
                            if (targetScrollY < 0) targetScrollY = 0;
                            scrollView.smoothScrollTo(0, targetScrollY);
                        }
                    }
                });
            }
        });
    }

    private void initAds() {
        MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    private boolean saveNote() {
        String title = (editBookTitle.getText() != null) ? editBookTitle.getText().toString().trim() : "";
        String content = (editContent.getText() != null) ? editContent.getText().toString().trim() : "";
        String remarks = (editRemarks.getText() != null) ? editRemarks.getText().toString().trim() : "";

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        if (currentItem == null || currentItem.getId() == 0) {
            ReadingNoteItem newItem = new ReadingNoteItem(title, content, remarks, now);
            dbHelper.addReadingNote(newItem);
        } else {
            currentItem.setBookTitle(title);
            currentItem.setContent(content);
            currentItem.setRemarks(remarks);
            currentItem.setModifiedDateTime(now);
            dbHelper.updateReadingNote(currentItem);
        }
        return true;
    }
}
