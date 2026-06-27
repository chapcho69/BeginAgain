package com.olivearchi.goodroutine;

import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import com.google.android.material.button.MaterialButton;

public class EnglishWordEditActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private EditText editWord, editMeaning, editExample;
    private NestedScrollView scrollView;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_english_word_edit);

        setSupportActionBar(findViewById(R.id.toolbar_english_edit));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new TodoDbHelper(this);
        editWord = findViewById(R.id.edit_english_word);
        editMeaning = findViewById(R.id.edit_english_meaning);
        editExample = findViewById(R.id.edit_english_example);
        scrollView = findViewById(R.id.scroll_english_edit);

        keepCursorCentered(editExample, scrollView);

        MaterialButton btnSave = findViewById(R.id.btn_english_save);
        btnSave.setOnClickListener(v -> saveWord());
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

    private void saveWord() {
        String word = editWord.getText().toString().trim();
        String meaning = editMeaning.getText().toString().trim();
        String example = editExample.getText().toString().trim();

        if (word.isEmpty() || meaning.isEmpty()) {
            Toast.makeText(this, "단어와 뜻을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        EnglishWordItem newItem = new EnglishWordItem(word, meaning, example, "", 1);
        dbHelper.addEnglishWord(newItem);
        Toast.makeText(this, "단어가 추가되었습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
