package com.olivearchi.goodroutine;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.olivearchi.goodroutine.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private TodoViewModel viewModel;
    private TextToSpeech tempTts;

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    restoreFromUri(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> saveFileLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/octet-stream"),
            uri -> {
                if (uri != null) {
                    saveToUri(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TodoViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.FirstFragment) {
                List<TodoItem> list = viewModel.getTodoList().getValue();
                updateAppTitle(list != null ? list.size() : 0);
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTaskDialog();
            }
        });

        viewModel.getTodoList().observe(this, habits -> {
            updateAppTitle(habits != null ? habits.size() : 0);
        });

        checkNotificationPermission();
        initAds();
        updateAppTitle();
    }

    private void updateAppTitle() {
        List<TodoItem> list = viewModel.getTodoList().getValue();
        int count = list != null ? list.size() : 0;
        String customName = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("appName", "Begin Again");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(customName + "(" + count + ")");
        }
    }

    private void updateAppTitle(int count) {
        String customName = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("appName", "Begin Again");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(customName + "(" + count + ")");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewModel != null) {
            viewModel.backupData();
        }
    }

    private void initAds() {
        MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void showAddTaskDialog() {
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

        repeatingCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repeatTypeSpinner.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        startEdit.setOnClickListener(v -> showDateTimePicker(startEdit));
        endEdit.setOnClickListener(v -> showDateTimePicker(endEdit));

        new AlertDialog.Builder(this)
                .setTitle("새 할 일 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
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
                        viewModel.addTodo(subject, detail, start, end, repeating, repeatType, done, color, emoticon);
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

    private void showDateTimePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            View timePickerView = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);
            NumberPicker hourPicker = timePickerView.findViewById(R.id.picker_hour);
            NumberPicker minutePicker = timePickerView.findViewById(R.id.picker_minute);

            hourPicker.setMinValue(0);
            hourPicker.setMaxValue(23);
            hourPicker.setValue(calendar.get(Calendar.HOUR_OF_DAY));

            String[] minutes = {"00", "10", "20", "30", "40", "50"};
            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(minutes.length - 1);
            minutePicker.setDisplayedValues(minutes);
            
            int currentMin = calendar.get(Calendar.MINUTE);
            minutePicker.setValue(currentMin / 10);

            new AlertDialog.Builder(this)
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_home) {
            Intent intent = new Intent(this, SelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_backup) {
            showBackupOptions();
            return true;
        } else if (id == R.id.action_restore) {
            showRestoreDialog();
            return true;
        } else if (id == R.id.action_rename_app) {
            showRenameAppDialog();
            return true;
        } else if (id == R.id.action_tts_speed) {
            showTtsSpeedDialog();
            return true;
        } else if (id == R.id.action_tts_voice) {
            showTtsVoiceDialog();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        int currentType = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("ttsVoiceType", 0); // 0: Female, 1: Male

        new AlertDialog.Builder(this)
                .setTitle("TTS 목소리 성별 선택")
                .setSingleChoiceItems(options, currentType, (dialog, which) -> {
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putInt("ttsVoiceType", which).apply();
                    dialog.dismiss();
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("ttsVoiceName", "").apply();
                    String msg = (which == 0) ? "여성 목소리로 설정되었습니다." : "남성 목소리로 설정되었습니다.";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton("목소리 상세 선택", (dialog, which) -> showAdvancedTtsVoiceDialog())
                .setNegativeButton("취소", null)
                .show();
    }



    private void showAdvancedTtsVoiceDialog() {
        if (tempTts != null) tempTts.shutdown();

        tempTts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Set<Voice> allVoices = tempTts.getVoices();
                List<Voice> koVoices = new ArrayList<>();
                if (allVoices != null) {
                    for (Voice v : allVoices) {
                        if (v.getLocale().getLanguage().equals("ko")) {
                            koVoices.add(v);
                        }
                    }
                }

                if (koVoices.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "선택 가능한 한국어 목소리가 없습니다.", Toast.LENGTH_SHORT).show());
                    return;
                }

                String[] voiceNames = new String[koVoices.size()];
                for (int i = 0; i < koVoices.size(); i++) {
                    voiceNames[i] = koVoices.get(i).getName();
                }

                String currentVoiceName = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("ttsVoiceName", "");
                int initialChecked = -1;
                for (int i = 0; i < voiceNames.length; i++) {
                    if (voiceNames[i].equals(currentVoiceName)) {
                        initialChecked = i;
                        break;
                    }
                }
                final int checkedItem = initialChecked;

                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("TTS 목소리 상세 선택")
                            .setSingleChoiceItems(voiceNames, checkedItem, (dialog, which) -> {
                                String selectedVoice = voiceNames[which];
                                getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("ttsVoiceName", selectedVoice).apply();
                                dialog.dismiss();
                                Toast.makeText(this, "목소리가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                            })
                            .setPositiveButton("시스템 설정", (dialog, which) -> {
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction("com.android.settings.TTS_SETTINGS");
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(this, "설정 화면을 열 수 없습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("취소", null)
                            .show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tempTts != null) tempTts.shutdown();
        super.onDestroy();
    }

    private void showBackupOptions() {
        String[] options = {"공유하기", "다른 위치에 저장하기"};
        new AlertDialog.Builder(this)
                .setTitle("데이터 보관 방식 선택")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        shareBackupFile();
                    } else {
                        saveFileLauncher.launch("todos_backup_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".dat");
                    }
                })
                .show();
    }

    private void shareBackupFile() {
        viewModel.backupData();
        File backupFile = new File(getCacheDir(), "todos_backup.dat");
        if (!backupFile.exists()) {
            Toast.makeText(this, "백업 파일 생성 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri contentUri = FileProvider.getUriForFile(this, "com.olivearchi.goodroutine.fileprovider", backupFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "좋은 루틴 - 데이터 백업");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "백업 파일 보내기"));
        } catch (Exception e) {
            Log.e("MainActivity", "Backup sharing failed", e);
            Toast.makeText(this, "백업 공유 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToUri(Uri uri) {
        viewModel.backupData();
        File internalBackup = new File(getCacheDir(), "todos_backup.dat");
        try (InputStream is = new FileInputStream(internalBackup);
             OutputStream os = getContentResolver().openOutputStream(uri)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MainActivity", "Save to URI failed", e);
            Toast.makeText(this, "저장 실패", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRestoreDialog() {
        new AlertDialog.Builder(this)
                .setTitle("데이터 복구")
                .setMessage("복구 시 현재 데이터가 모두 삭제되고 백업 파일의 데이터로 대체됩니다. 계속할까요?")
                .setPositiveButton("파일 선택", (dialog, which) -> {
                    filePickerLauncher.launch(new String[]{"application/octet-stream", "*/*"});
                })
                .setNegativeButton("취소", null)
                .show();
    }

    @SuppressWarnings("unchecked")
    private void restoreFromUri(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            Object data = ois.readObject();
            if (data != null) {
                viewModel.restoreData(data);
                Toast.makeText(this, "데이터 복구 성공", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Restore failed", e);
            Toast.makeText(this, "복구 실패: 올바른 백업 파일이 아닙니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        String version = getString(R.string.app_version);
        String buildDate = getString(R.string.build_date);
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("버전: " + version + "\n마지막 빌드: " + buildDate + "\n제작자 정보: Routine Maker\n문의: managedswsvc@gmail.com")
                .setPositiveButton("확인", null)
                .show();
    }

    private void showRenameAppDialog() {
        EditText input = new EditText(this);
        String currentName = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("appName", "Begin Again");
        input.setText(currentName);

        new AlertDialog.Builder(this)
                .setTitle("App 명칭 수정")
                .setView(input)
                .setPositiveButton("저장", (dialog, which) -> {
                    String newName = input.getText().toString();
                    if (!newName.isEmpty()) {
                        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("appName", newName).apply();
                        updateAppTitle();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void exportTodoList() {
        List<TodoItem> todos = viewModel.getTodoList().getValue();
        if (todos == null || todos.isEmpty()) {
            Toast.makeText(this, "내보낼 할 일이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File exportFile = new File(getCacheDir(), "todo_list.txt");
            FileWriter writer = new FileWriter(exportFile);
            
            writer.write("루틴 만들기 - 할 일 목록\n");
            writer.write("==============================\n\n");
            
            for (TodoItem todo : todos) {
                writer.write("제목: " + todo.getSubject() + "\n");
                writer.write("내용: " + todo.getDetail() + "\n");
                writer.write("시작: " + todo.getStartDateTime() + "\n");
                writer.write("종료: " + todo.getEndDateTime() + "\n");
                writer.write("상태: " + (todo.isDone() ? "완료" : "미완료") + "\n");
                writer.write("반복: " + (todo.isRepeating() ? "예" : "아니오") + "\n");
                writer.write("------------------------------\n");
            }
            writer.close();

            Uri contentUri = FileProvider.getUriForFile(this, "com.olivearchi.goodroutine.fileprovider", exportFile);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "좋은 루틴 - 할 일 목록 내보내기");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "목록 보내기"));

        } catch (Exception e) {
            Log.e("MainActivity", "Export failed", e);
            Toast.makeText(this, "내보내기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
