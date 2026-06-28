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

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

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
        ThemeHelper.applyTheme(this);
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
                updateAppTitle();
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddTaskDialog();
            }
        });

        viewModel.getTodoList().observe(this, habits -> {
            updateAppTitle();
        });

        checkNotificationPermission();
        initAds();
        updateAppTitle();
    }

    private void updateAppTitle() {
        List<TodoItem> list = viewModel.getTodoList().getValue();
        int count = list != null ? list.size() : 0;
        String customName = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("appName", getString(R.string.app_name));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(customName + "(" + count + ")");
        }
    }

    private void initAds() {
        com.google.android.gms.ads.MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.setAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdFailedToLoad(@androidx.annotation.NonNull com.google.android.gms.ads.LoadAdError adError) {
                    adView.setVisibility(View.GONE);
                }
                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }
            });
            com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
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

    @Override
    protected void onResume() {
        super.onResume();
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.resume();
        updateAppTitle();
    }

    @Override
    protected void onPause() {
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.pause();
        super.onPause();
        if (viewModel != null) {
            viewModel.backupData();
        }
    }

    @Override
    protected void onDestroy() {
        AdView adView = findViewById(R.id.adView);
        if (adView != null) adView.destroy();
        if (tempTts != null) tempTts.shutdown();
        super.onDestroy();
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
                .setTitle(R.string.msg_add_todo_title)
                .setView(dialogView)
                .setPositiveButton(R.string.btn_add, (dialog, which) -> {
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
                .setNegativeButton(R.string.button_cancel, null)
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
                    .setTitle(R.string.msg_time_select)
                    .setView(timePickerView)
                    .setPositiveButton(R.string.button_select, (dialog, which) -> {
                        int hour = hourPicker.getValue();
                        int minute = Integer.parseInt(minutes[minutePicker.getValue()]);
                        
                        String dateTime = String.format(Locale.getDefault(), "%d-%02d-%02d %02d:%02d",
                                year, month + 1, dayOfMonth, hour, minute);
                        editText.setText(dateTime);
                    })
                    .setNegativeButton(R.string.button_cancel, null)
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
                .setTitle(R.string.action_tts_speed)
                .setView(layout)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putFloat("ttsSpeed", speed[0]).apply();
                    String msg = String.format(Locale.getDefault(), getString(R.string.msg_tts_voice_set) + " (%.1f)", speed[0]);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void showTtsVoiceDialog() {
        String[] options = {getString(R.string.label_female_voice), getString(R.string.label_male_voice)};
        int currentType = getSharedPreferences("AppPrefs", MODE_PRIVATE).getInt("ttsVoiceType", 0); // 0: Female, 1: Male

        new AlertDialog.Builder(this)
                .setTitle(R.string.msg_tts_gender_select)
                .setSingleChoiceItems(options, currentType, (dialog, which) -> {
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putInt("ttsVoiceType", which).apply();
                    dialog.dismiss();
                    getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("ttsVoiceName", "").apply();
                    String msg = (which == 0) ? getString(R.string.msg_tts_voice_set_female) : getString(R.string.msg_tts_voice_set_male);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton(R.string.btn_advanced_tts, (dialog, which) -> showAdvancedTtsVoiceDialog())
                .setNegativeButton(R.string.button_cancel, null)
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
                    runOnUiThread(() -> Toast.makeText(this, R.string.msg_no_tts_voices, Toast.LENGTH_SHORT).show());
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
                            .setTitle(R.string.msg_advanced_tts_title)
                            .setSingleChoiceItems(voiceNames, checkedItem, (dialog, which) -> {
                                String selectedVoice = voiceNames[which];
                                getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("ttsVoiceName", selectedVoice).apply();
                                dialog.dismiss();
                                Toast.makeText(this, R.string.msg_tts_voice_set, Toast.LENGTH_SHORT).show();
                            })
                            .setPositiveButton(R.string.btn_system_settings, (dialog, which) -> {
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction("com.android.settings.TTS_SETTINGS");
                                    startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(this, R.string.msg_fail_open_settings, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton(R.string.button_cancel, null)
                            .show();
                });
            }
        });
    }

    private void showBackupOptions() {
        String[] options = {getString(R.string.btn_chooser_share), getString(R.string.btn_chooser_save)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.msg_backup_method)
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
            Toast.makeText(this, R.string.msg_backup_fail, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri contentUri = FileProvider.getUriForFile(this, "com.olivearchi.goodroutine.fileprovider", backupFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " - " + getString(R.string.action_backup));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_backup)));
        } catch (Exception e) {
            Log.e("MainActivity", "Backup sharing failed", e);
            Toast.makeText(this, R.string.msg_backup_fail, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, R.string.msg_save_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MainActivity", "Save to URI failed", e);
            Toast.makeText(this, R.string.msg_export_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void showRestoreDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_restore)
                .setMessage(R.string.msg_restore_confirm)
                .setPositiveButton(R.string.btn_chooser_file, (dialog, which) -> {
                    filePickerLauncher.launch(new String[]{"application/octet-stream", "*/*"});
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @SuppressWarnings("unchecked")
    private void restoreFromUri(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            Object data = ois.readObject();
            if (data != null) {
                viewModel.restoreData(data);
                Toast.makeText(this, R.string.msg_restore_success, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Restore failed", e);
            Toast.makeText(this, R.string.msg_restore_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void showAboutDialog() {
        String version = getString(R.string.app_version);
        String buildDate = getString(R.string.build_date);
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("버전: " + version + "\n마지막 빌드: " + buildDate + "\n제작자 정보: Routine Maker\n문의: managedswsvc@gmail.com")
                .setPositiveButton(R.string.button_close, null)
                .show();
    }

    private void showRenameAppDialog() {
        EditText input = new EditText(this);
        String currentName = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("appName", "Begin Again");
        input.setText(currentName);

        new AlertDialog.Builder(this)
                .setTitle(R.string.action_rename_app)
                .setView(input)
                .setPositiveButton(R.string.btn_save, (dialog, which) -> {
                    String newName = input.getText().toString();
                    if (!newName.isEmpty()) {
                        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().putString("appName", newName).apply();
                        updateAppTitle();
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
