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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MemorizationEditActivity extends AppCompatActivity {

    private TodoDbHelper dbHelper;
    private MemorizationItem currentItem;
    private TextInputEditText editTitle, editKeyword, editContent;
    private TextInputLayout layoutTitle, layoutKeyword, layoutContent;
    private NestedScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorization_edit);

        dbHelper = new TodoDbHelper(this);
        
        editTitle = findViewById(R.id.edit_memo_title);
        editKeyword = findViewById(R.id.edit_memo_keyword);
        editContent = findViewById(R.id.edit_memo_content);
        scrollView = findViewById(R.id.scroll_memorization_edit);

        layoutTitle = (TextInputLayout) editTitle.getParent().getParent();
        layoutKeyword = (TextInputLayout) editKeyword.getParent().getParent();
        layoutContent = (TextInputLayout) editContent.getParent().getParent();

        setupVerticalScroll(editContent);
        keepCursorCentered(editContent, scrollView);

        MaterialButton btnSave = findViewById(R.id.btn_memo_save);
        MaterialButton btnAddNext = findViewById(R.id.btn_memo_add_next);
        MaterialButton btnDelete = findViewById(R.id.btn_memo_delete);

        currentItem = (MemorizationItem) getIntent().getSerializableExtra("memo_item");

        android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        editContent.getLayoutParams().height = (int)(screenHeight * 0.5);
        editContent.requestLayout();

        if (currentItem != null && currentItem.getId() != 0) {
            editTitle.setText(currentItem.getTitle());
            editKeyword.setText(currentItem.getKeyword());
            editContent.setText(currentItem.getContent());
            btnDelete.setVisibility(View.VISIBLE);
        } else if (currentItem != null && currentItem.getId() == 0) {
            editKeyword.setText(currentItem.getKeyword());
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // Setup ByteCounter AFTER setting initial text
        setupByteCounter(editTitle, layoutTitle, 100, "제목");
        setupByteCounter(editKeyword, layoutKeyword, 200, "주제어");
        setupByteCounter(editContent, layoutContent, 2000, "내용");

        btnSave.setOnClickListener(v -> {
            if (saveMemo()) finish();
        });

        btnAddNext.setOnClickListener(v -> {
            if (saveMemo()) {
                String keyword = (editKeyword.getText() != null) ? editKeyword.getText().toString() : "";
                Intent intent = new Intent(this, MemorizationEditActivity.class);
                MemorizationItem template = new MemorizationItem("", "", keyword, "", "");
                intent.putExtra("memo_item", template);
                startActivity(intent);
                finish();
            }
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("삭제 확인")
                    .setMessage("정말 삭제 할까요?")
                    .setPositiveButton("예", (dialog, which) -> {
                        dbHelper.deleteMemorization(currentItem.getId());
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
                int destBytes = dest.toString().getBytes().length;
                int sourceBytes = source.subSequence(start, end).toString().getBytes().length;
                int replacedBytes = dest.subSequence(dstart, dend).toString().getBytes().length;
                if (destBytes + sourceBytes - replacedBytes > maxBytes) {
                    if (dest.length() == 0 && source.length() > 0 && sourceBytes > maxBytes) return null;
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
                int bytes = s.toString().getBytes().length;
                int percent = Math.min(100, (int)((bytes / (float)maxBytes) * 100));
                layout.setHint(baseHint + " (" + percent + "% 사용 중)");
            }
        });
        int initialBytes = (editText.getText() != null) ? editText.getText().toString().getBytes().length : 0;
        int initialPercent = Math.min(100, (int)((initialBytes / (float)maxBytes) * 100));
        layout.setHint(baseHint + " (" + initialPercent + "% 사용 중)");
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

                        int[] viewLocation = new int[2];
                        int[] scrollLocation = new int[2];
                        editText.getLocationInWindow(viewLocation);
                        scrollView.getLocationInWindow(scrollLocation);

                        int cursorYInWindow = viewLocation[1] + lineTop;
                        int scrollYInWindow = scrollLocation[1];
                        int cursorYInScroll = cursorYInWindow - scrollYInWindow + scrollView.getScrollY();

                        int scrollHeight = scrollView.getHeight();
                        if (scrollHeight > 0) {
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

    private boolean saveMemo() {
        String title = (editTitle.getText() != null) ? editTitle.getText().toString().trim() : "";
        String keyword = (editKeyword.getText() != null) ? editKeyword.getText().toString().trim() : "";
        String content = (editContent.getText() != null) ? editContent.getText().toString().trim() : "";

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        if (currentItem == null || currentItem.getId() == 0) {
            MemorizationItem newItem = new MemorizationItem(title, content, keyword, now, now);
            dbHelper.addMemorization(newItem);
        } else {
            currentItem.setTitle(title);
            currentItem.setKeyword(keyword);
            currentItem.setContent(content);
            currentItem.setUpdatedAt(now);
            dbHelper.updateMemorization(currentItem);
        }
        return true;
    }
}
