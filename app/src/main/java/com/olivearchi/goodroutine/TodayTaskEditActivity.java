package com.olivearchi.goodroutine;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import androidx.core.widget.NestedScrollView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class TodayTaskEditActivity extends AppCompatActivity {

    private TodoDbHelper dbHelper;
    private TodayTaskItem currentItem;
    private TextInputEditText editTitle, editDescription;
    private NumberPicker pickerMinutes;
    private NestedScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_task_edit);

        dbHelper = new TodoDbHelper(this);
        editTitle = findViewById(R.id.edit_task_title);
        editDescription = findViewById(R.id.edit_task_description);
        scrollView = findViewById(R.id.scroll_task_edit);

        TextInputLayout layoutTitle = (TextInputLayout) editTitle.getParent().getParent();
        TextInputLayout layoutDescription = (TextInputLayout) editDescription.getParent().getParent();

        setupVerticalScroll(editDescription);
        keepCursorCentered(editDescription, scrollView);

        pickerMinutes = findViewById(R.id.picker_task_minutes);
        MaterialButton btnSave = findViewById(R.id.btn_save_today_task);
        MaterialButton btnDelete = findViewById(R.id.btn_delete_today_task);

        // Setup NumberPicker for 10-minute increments
        String[] displayValues = new String[25]; // 0 to 240 minutes
        for (int i = 0; i < displayValues.length; i++) {
            displayValues[i] = String.valueOf(i * 10);
        }
        pickerMinutes.setMinValue(0);
        pickerMinutes.setMaxValue(displayValues.length - 1);
        pickerMinutes.setDisplayedValues(displayValues);

        currentItem = (TodayTaskItem) getIntent().getSerializableExtra("task_item");

        android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        editDescription.getLayoutParams().height = (int)(screenHeight * 0.5);
        editDescription.requestLayout();

        if (currentItem != null) {
            editTitle.setText(currentItem.getTitle());
            editDescription.setText(currentItem.getDescription());
            pickerMinutes.setValue(currentItem.getEstimatedMinutes() / 10);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnDelete.setVisibility(View.GONE);
        }

        // Setup ByteCounter AFTER setting initial text
        setupByteCounter(editTitle, layoutTitle, 100, "업무 제목");
        setupByteCounter(editDescription, layoutDescription, 2000, "업무 설명");

        btnSave.setOnClickListener(v -> saveTask());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("삭제 확인")
                    .setMessage("삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        dbHelper.deleteTodayTask(currentItem.getId());
                        Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("취소", null)
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

    private void saveTask() {
        String title = (editTitle.getText() != null) ? editTitle.getText().toString().trim() : "";
        String description = (editDescription.getText() != null) ? editDescription.getText().toString().trim() : "";
        int minutes = pickerMinutes.getValue() * 10;

        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentItem == null) {
            TodayTaskItem newItem = new TodayTaskItem(title, description, minutes);
            dbHelper.addTodayTask(newItem);
        } else {
            currentItem.setTitle(title);
            currentItem.setDescription(description);
            currentItem.setEstimatedMinutes(minutes);
            dbHelper.updateTodayTask(currentItem);
        }

        finish();
    }
}
