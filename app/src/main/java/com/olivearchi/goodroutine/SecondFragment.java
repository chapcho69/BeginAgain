package com.olivearchi.goodroutine;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.content.ContextCompat;
import android.widget.ScrollView;
import android.widget.TextView;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.olivearchi.goodroutine.databinding.FragmentSecondBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private TodoViewModel viewModel;
    private int todoPosition = -1;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

        if (getArguments() != null) {
            todoPosition = getArguments().getInt("todoPosition", -1);
        }

        if (todoPosition == -1 || viewModel.getTodoList().getValue() == null || todoPosition >= viewModel.getTodoList().getValue().size()) {
            Toast.makeText(requireContext(), "항목을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        updateUI();

        TodoItem item = viewModel.getTodoList().getValue().get(todoPosition);
        if (getActivity() != null && getActivity() instanceof androidx.appcompat.app.AppCompatActivity) {
            androidx.appcompat.app.ActionBar actionBar = ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(item.getEmoticon() + " " + item.getSubject());
            }
        }

        binding.buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("삭제")
                    .setMessage("삭제할까요?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        viewModel.deleteTodo(todoPosition);
                        NavHostFragment.findNavController(this).navigateUp();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        binding.buttonEdit.setOnClickListener(v -> {
            showEditDialog();
        });

        binding.buttonRecord.setOnClickListener(v -> {
            TodoItem currentItem = viewModel.getTodoList().getValue().get(todoPosition);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            viewModel.addPerformHistory(currentItem, timestamp);
            updateUI();
            Toast.makeText(requireContext(), "수행 기록이 추가되었습니다.", Toast.LENGTH_SHORT).show();
        });

        binding.buttonViewHistory.setOnClickListener(v -> {
            showAllHistoryDialog();
        });
    }

    private void showAllHistoryDialog() {
        if (viewModel.getTodoList().getValue() == null || todoPosition >= viewModel.getTodoList().getValue().size()) return;
        
        TodoItem item = viewModel.getTodoList().getValue().get(todoPosition);
        List<String> allHistory = viewModel.getAllHistory(item.getId());
        
        StringBuilder sb = new StringBuilder();
        for (String h : allHistory) {
            sb.append(h).append("\n");
        }
        
        if (sb.length() == 0) {
            sb.append("수행 기록이 없습니다.");
        }

        // Explicitly create a ScrollView with a TextView to ensure scrollability as requested
        TextView textView = new TextView(requireContext());
        textView.setText(sb.toString());
        textView.setPadding(48, 24, 48, 24);
        textView.setTextSize(16);
        
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.addView(textView);

        new AlertDialog.Builder(requireContext())
                .setTitle("전체 수행 이력")
                .setView(scrollView)
                .setPositiveButton("닫기", null)
                .show();
    }

    private void updateUI() {
        if (viewModel.getTodoList().getValue() == null || todoPosition >= viewModel.getTodoList().getValue().size()) return;

        TodoItem item = viewModel.getTodoList().getValue().get(todoPosition);
        binding.textviewSubject.setText(item.getEmoticon() + " " + item.getSubject());
        
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        if (item.getDetail() != null && !item.getDetail().isEmpty()) {
            ssb.append(item.getDetail()).append("\n\n");
        }
        
        ssb.append("[기록]\n");
        int checkCount = Math.min(item.getPerformCount(), 100);
        Drawable checkIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_circle);
        if (checkIcon != null) {
            checkIcon = DrawableCompat.wrap(checkIcon.mutate());
            DrawableCompat.setTint(checkIcon, 0xFF2196F3); // Blue
            int size = (int) (binding.textviewDetail.getTextSize() * 1.2);
            checkIcon.setBounds(0, 0, size, size);
            
            for (int i = 0; i < checkCount; i++) {
                ssb.append(" ");
                ssb.setSpan(new ImageSpan(checkIcon, ImageSpan.ALIGN_BOTTOM), ssb.length() - 1, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                // Add a newline after every 15 icons as requested
                if ((i + 1) % 15 == 0 && i < checkCount - 1) {
                    ssb.append("\n");
                }
            }
        }
        
        binding.textviewDetail.setText(ssb);
        binding.textviewDatetime.setText(String.format("시작: %s\n종료: %s", 
                item.getStartDateTime(), item.getEndDateTime()));
        
        String repeatStr = "아니오";
        if (item.isRepeating()) {
            String[] types = getResources().getStringArray(R.array.repeat_types);
            if (item.getRepeatType() > 0 && item.getRepeatType() <= types.length) {
                repeatStr = types[item.getRepeatType() - 1];
            }
        }

        binding.textviewRepeating.setText(String.format("반복: %s | 완료: %s | 수행: %d회",
                repeatStr,
                item.isDone() ? "완료" : "미완료",
                item.getPerformCount()));
        
        int backgroundColor = item.getColor();
        binding.getRoot().setBackgroundColor(backgroundColor);
        
        int textColor = ColorUtils.getContrastColor(backgroundColor);
        binding.textviewSubject.setTextColor(textColor);
        binding.textviewDetail.setTextColor(textColor);
        binding.textviewDatetime.setTextColor(textColor);
        binding.textviewRepeating.setTextColor(textColor);
    }

    private void showEditDialog() {
        if (viewModel.getTodoList().getValue() == null || todoPosition >= viewModel.getTodoList().getValue().size()) return;

        TodoItem item = viewModel.getTodoList().getValue().get(todoPosition);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_todo, null);
        
        EditText subjectEdit = dialogView.findViewById(R.id.edit_subject);
        EditText detailEdit = dialogView.findViewById(R.id.edit_detail);
        setupVerticalScroll(detailEdit);
        EditText startEdit = dialogView.findViewById(R.id.edit_start_datetime);
        EditText endEdit = dialogView.findViewById(R.id.edit_end_datetime);
        CheckBox repeatingCheck = dialogView.findViewById(R.id.check_repeating);
        Spinner repeatTypeSpinner = dialogView.findViewById(R.id.spinner_repeat_type);
        CheckBox doneCheck = dialogView.findViewById(R.id.check_done);
        Spinner emoticonSpinner = dialogView.findViewById(R.id.spinner_emoticon);
        RadioGroup colorGroup = dialogView.findViewById(R.id.radiogroup_colors);

        subjectEdit.setText(item.getSubject());
        detailEdit.setText(item.getDetail());
        startEdit.setText(item.getStartDateTime());
        endEdit.setText(item.getEndDateTime());
        repeatingCheck.setChecked(item.isRepeating());
        repeatTypeSpinner.setVisibility(item.isRepeating() ? View.VISIBLE : View.GONE);
        if (item.isRepeating() && item.getRepeatType() > 0) {
            repeatTypeSpinner.setSelection(item.getRepeatType() - 1);
        }
        doneCheck.setChecked(item.isDone());
        setSelectedColor(colorGroup, item.getColor());

        // Set emoticon selection
        String[] emoticons = getResources().getStringArray(R.array.habit_emoticons);
        for (int i = 0; i < emoticons.length; i++) {
            if (emoticons[i].equals(item.getEmoticon())) {
                emoticonSpinner.setSelection(i);
                break;
            }
        }

        repeatingCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repeatTypeSpinner.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        startEdit.setOnClickListener(v -> showDateTimePicker(startEdit));
        endEdit.setOnClickListener(v -> showDateTimePicker(endEdit));

        new AlertDialog.Builder(requireContext())
                .setTitle("할 일 수정")
                .setView(dialogView)
                .setPositiveButton("저장", (dialog, which) -> {
                    String subject = subjectEdit.getText().toString();
                    String detail = detailEdit.getText().toString();
                    String start = startEdit.getText().toString();
                    String end = endEdit.getText().toString();
                    boolean repeating = repeatingCheck.isChecked();
                    int repeatType = repeating ? (repeatTypeSpinner.getSelectedItemPosition() + 1) : 0;
                    boolean done = doneCheck.isChecked();
                    int color = getSelectedColor(colorGroup);
                    String emoticon = emoticonSpinner.getSelectedItem().toString();

                    if (!subject.isEmpty()) {
                        viewModel.updateTodo(todoPosition, subject, detail, start, end, repeating, repeatType, done, color, emoticon);
                        updateUI();
                        // Also update action bar title
                        if (getActivity() != null && getActivity() instanceof androidx.appcompat.app.AppCompatActivity) {
                            androidx.appcompat.app.ActionBar actionBar = ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar();
                            if (actionBar != null) {
                                actionBar.setTitle(emoticon + " " + subject);
                            }
                        }
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void setupVerticalScroll(View view) {
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

    private int getSelectedColor(RadioGroup colorGroup) {
        int checkedId = colorGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_red) return 0xFFFFCDD2;
        if (checkedId == R.id.radio_blue) return 0xFFBBDEFB;
        if (checkedId == R.id.radio_green) return 0xFFC8E6C9;
        if (checkedId == R.id.radio_yellow) return 0xFFFFF9C4;
        return 0xFFFFFFFF; // Default white
    }

    private void setSelectedColor(RadioGroup colorGroup, int color) {
        if (color == 0xFFFFCDD2) colorGroup.check(R.id.radio_red);
        else if (color == 0xFFBBDEFB) colorGroup.check(R.id.radio_blue);
        else if (color == 0xFFC8E6C9) colorGroup.check(R.id.radio_green);
        else if (color == 0xFFFFF9C4) colorGroup.check(R.id.radio_yellow);
        else colorGroup.check(R.id.radio_white);
    }

    private void showDateTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            View timePickerView = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);
            android.widget.NumberPicker hourPicker = timePickerView.findViewById(R.id.picker_hour);
            android.widget.NumberPicker minutePicker = timePickerView.findViewById(R.id.picker_minute);

            hourPicker.setMinValue(0);
            hourPicker.setMaxValue(23);
            hourPicker.setValue(calendar.get(Calendar.HOUR_OF_DAY));

            String[] minutes = {"00", "10", "20", "30", "40", "50"};
            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(minutes.length - 1);
            minutePicker.setDisplayedValues(minutes);
            
            int currentMin = calendar.get(Calendar.MINUTE);
            minutePicker.setValue(currentMin / 10);

            new AlertDialog.Builder(requireContext())
                    .setTitle("시간 선택 (10분 단위)")
                    .setView(timePickerView)
                    .setPositiveButton("선택", (dialog, which) -> {
                        int hour = hourPicker.getValue();
                        int minute = Integer.parseInt(minutes[minutePicker.getValue()]);
                        
                        String dateTime = String.format(Locale.getDefault(), "%d-%02d-%02d %02d:%02d",
                                year, month + 1, dayOfMonth, hour, minute);
                        editText.setText(dateTime);
                    })
                    .setNegativeButton("취소", null)
                    .show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
