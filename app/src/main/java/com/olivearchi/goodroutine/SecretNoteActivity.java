package com.olivearchi.goodroutine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SecretNoteActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    private TodoDbHelper dbHelper;
    private SecretNoteAdapter adapter;
    private RecyclerView recyclerView;
    private List<SecretNoteItem> currentNotes;
    private boolean isFavoriteFilterActive = false;
    private boolean isAuthenticated = false;

    private static final String PREFS_NAME = "SecretPrefs";
    private static final String KEY_PIN = "secret_pin";
    private static final String KEY_FAILED_ATTEMPTS = "failed_attempts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_note);

        setSupportActionBar(findViewById(R.id.toolbar_secret_list));
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new TodoDbHelper(this);
        recyclerView = findViewById(R.id.recycler_secret_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab_add_secret_note);
        fab.setOnClickListener(v -> {
            if (isAuthenticated) startActivity(new Intent(this, SecretNoteEditActivity.class));
        });

        initAds();

        if (savedInstanceState != null) {
            isAuthenticated = savedInstanceState.getBoolean("auth", false);
        }

        if (!isAuthenticated) {
            showPinEntryDialog();
        } else {
            loadSecretNotes();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("auth", isAuthenticated);
    }

    private void showPinEntryDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedPin = prefs.getString(KEY_PIN, null);
        int failedCount = prefs.getInt(KEY_FAILED_ATTEMPTS, 0);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pin_entry, null);
        TextView tvMsg = dialogView.findViewById(R.id.text_pin_message);
        EditText etPin = dialogView.findViewById(R.id.edit_pin_input);
        TextView tvError = dialogView.findViewById(R.id.text_pin_error);

        boolean isSetup = (savedPin == null);
        if (isSetup) {
            tvMsg.setText(R.string.msg_pin_setup);
        } else {
            tvMsg.setText(String.format(Locale.getDefault(), getString(R.string.msg_pin_entry), failedCount));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton(R.string.button_cancel, (d, w) -> finish())
                .create();

        etPin.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4) {
                    String input = s.toString();
                    if (isSetup) {
                        prefs.edit().putString(KEY_PIN, input).putInt(KEY_FAILED_ATTEMPTS, 0).apply();
                        Toast.makeText(SecretNoteActivity.this, R.string.msg_pin_set_success, Toast.LENGTH_SHORT).show();
                        isAuthenticated = true;
                        dialog.dismiss();
                        loadSecretNotes();
                    } else {
                        if (input.equals(savedPin)) {
                            prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).apply();
                            isAuthenticated = true;
                            dialog.dismiss();
                            loadSecretNotes();
                        } else {
                            int newFailed = failedCount + 1;
                            if (newFailed >= 10) {
                                prefs.edit().remove(KEY_PIN).putInt(KEY_FAILED_ATTEMPTS, 0).apply();
                                Toast.makeText(SecretNoteActivity.this, R.string.msg_pin_reset, Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                                showPinEntryDialog();
                            } else {
                                prefs.edit().putInt(KEY_FAILED_ATTEMPTS, newFailed).apply();
                                tvError.setText(String.format(Locale.getDefault(), getString(R.string.msg_pin_wrong), newFailed));
                                tvError.setVisibility(View.VISIBLE);
                                s.clear();
                            }
                        }
                    }
                }
            }
        });

        dialog.show();
    }

    private void loadSecretNotes() {
        List<SecretNoteItem> all = dbHelper.getAllSecretNotes();
        currentNotes = new ArrayList<>();
        if (isFavoriteFilterActive) {
            for (SecretNoteItem i : all) if (i.isFavorite()) currentNotes.add(i);
        } else {
            currentNotes.addAll(all);
        }

        adapter = new SecretNoteAdapter(currentNotes, item -> {
            Intent intent = new Intent(this, SecretNoteDetailActivity.class);
            intent.putExtra("note_item", item);
            startActivity(intent);
        }, (item, newState) -> {
            dbHelper.toggleSecretNoteFavorite(item.getId(), newState);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override protected void onResume() { super.onResume(); if (isAuthenticated) loadSecretNotes(); }
    @Override public boolean onCreateOptionsMenu(Menu menu) { 
        getMenuInflater().inflate(R.menu.menu_list, menu); 
        return true; 
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) { finish(); return true; }
        if (id == R.id.action_home) { 
            Intent intent = new Intent(this, SelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_filter_favorite) {
            isFavoriteFilterActive = !isFavoriteFilterActive;
            invalidateOptionsMenu();
            loadSecretNotes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initAds() {
        com.google.android.gms.ads.MobileAds.initialize(this, initializationStatus -> {});
        AdView adView = findViewById(R.id.adView);
        if (adView != null) {
            adView.setAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdFailedToLoad(@androidx.annotation.NonNull com.google.android.gms.ads.LoadAdError adError) {
                    adView.setVisibility(android.view.View.GONE);
                }
                @Override
                public void onAdLoaded() {
                    adView.setVisibility(android.view.View.VISIBLE);
                }
            });
            com.google.android.gms.ads.AdRequest adRequest = new com.google.android.gms.ads.AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
    }
}
