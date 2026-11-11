package com.example.lab4.presentation.detail;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.lab4.domain.model.Recipe;
import com.example.lab4.presentation.common.AppGraph;
import com.example.lab4.presentation.common.Ui;
import com.example.lab4.presentation.photo.PhotoActivity;
import java.io.File;
import java.util.Calendar;

public class DetailActivity extends AppCompatActivity {
    private long id;
    private Recipe recipe;
    private Button calendarButton;
    private ActivityResultLauncher<String[]> permissionRequest;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); id = getIntent().getLongExtra("id", -1);
        permissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (hasCalendarPermission()) pickDate();
            else Toast.makeText(this, "Доступ к календарю отклонён. Рецепт доступен без него.", Toast.LENGTH_LONG).show();
        });
    }
    @Override protected void onResume() {
        super.onResume();
        AppGraph.IO.execute(() -> {
            Recipe result = AppGraph.actions(this).get(id);
            runOnUiThread(() -> { if (result == null) { finish(); return; } recipe = result; showRecipe(); });
        });
    }
    private void showRecipe() {
        getWindow().setStatusBarColor(Ui.CREAM);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        ScrollView scroll = new ScrollView(this); LinearLayout root = Ui.column(this); scroll.addView(root);
        com.google.android.material.button.MaterialButton back = Ui.backButton(this);
        root.addView(back, new LinearLayout.LayoutParams(-2, Ui.dp(this, 52))); back.setOnClickListener(v -> finish()); Ui.space(root, 18);
        ImageView image = new ImageView(this); image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackground(Ui.rounded(this, Ui.SAND, 22)); image.setClipToOutline(true);
        root.addView(image, new LinearLayout.LayoutParams(-1, Ui.dp(this, 270)));
        Glide.with(this).load(new File(recipe.imagePath)).override(900, 900).centerCrop().into(image);
        image.setContentDescription("Открыть фотографию блюда крупно");
        image.setOnClickListener(v -> startActivity(new Intent(this, PhotoActivity.class).putExtra("path", recipe.imagePath)));
        Ui.space(root, 9); android.widget.TextView enlarge = Ui.muted(this, "Нажмите на фото, чтобы увеличить", 13);
        enlarge.setGravity(android.view.Gravity.CENTER); root.addView(enlarge);
        Ui.space(root, 19); android.widget.TextView category = Ui.muted(this, recipe.category.toUpperCase(java.util.Locale.ROOT), 12);
        category.setLetterSpacing(.12f); root.addView(category); Ui.space(root, 6);
        root.addView(Ui.text(this, recipe.title, 30, true)); Ui.space(root, 13);
        android.widget.TextView duration = Ui.text(this, "◷   " + recipe.minutes + " минут приготовления", 15, false);
        duration.setTextColor(Ui.ACCENT); root.addView(duration);
        Ui.space(root, 26);
        section(root, "Ингредиенты", recipe.ingredients);
        Ui.space(root, 16);
        section(root, "Приготовление", recipe.steps);
        Ui.space(root, 28);
        root.addView(Ui.text(this, "План готовки", 20, true)); Ui.space(root, 7);
        root.addView(Ui.muted(this, recipe.calendarEventId == null
                ? "Добавьте блюдо в календарь и получите напоминание за час."
                : "Для этого рецепта уже есть событие. Можно выбрать новую дату.", 15));
        Ui.space(root, 13);
        calendarButton = Ui.button(this, recipe.calendarEventId == null ? "Запланировать готовку" : "Изменить дату готовки");
        root.addView(calendarButton, new LinearLayout.LayoutParams(-1, Ui.dp(this, 56)));
        calendarButton.setOnClickListener(v -> {
            if (hasCalendarPermission()) pickDate(); else permissionRequest.launch(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR});
        });
        Ui.space(root, 20);
        com.google.android.material.button.MaterialButton delete = new com.google.android.material.button.MaterialButton(this);
        delete.setText("Удалить рецепт"); delete.setAllCaps(false); delete.setTextColor(Ui.ACCENT);
        delete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.SAND));
        delete.setCornerRadius(Ui.dp(this, 16)); root.addView(delete, new LinearLayout.LayoutParams(-1, Ui.dp(this, 52)));
        delete.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Удалить рецепт?")
                .setMessage("Фотография и связанное событие календаря тоже будут удалены.")
                .setNegativeButton("Отмена", null).setPositiveButton("Удалить", (d, w) -> deleteRecipe()).show());
        setContentView(scroll);
    }
    private void section(LinearLayout root, String heading, String body) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(Ui.dp(this, 18), Ui.dp(this, 17), Ui.dp(this, 18), Ui.dp(this, 18));
        card.setBackground(Ui.rounded(this, 0xffffffff, 20));
        card.addView(Ui.text(this, heading, 20, true)); Ui.space(card, 11);
        android.widget.TextView content = Ui.text(this, body, 16, false); content.setLineSpacing(Ui.dp(this, 4), 1f);
        card.addView(content); root.addView(card);
    }
    private boolean hasCalendarPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }
    private void pickDate() {
        Calendar day = Calendar.getInstance();
        new DatePickerDialog(this, (picker, year, month, date) -> {
            day.set(year, month, date);
            new TimePickerDialog(this, (timePicker, hour, minute) -> {
                day.set(Calendar.HOUR_OF_DAY, hour); day.set(Calendar.MINUTE, minute); day.set(Calendar.SECOND, 0);
                if (day.getTimeInMillis() <= System.currentTimeMillis()) {
                    Toast.makeText(this, "Выберите будущую дату и время", Toast.LENGTH_LONG).show(); return;
                }
                CheckBox weekly = new CheckBox(this); weekly.setText("Повторять каждую неделю");
                LinearLayout options = new LinearLayout(this); options.setOrientation(LinearLayout.VERTICAL);
                options.setPadding(Ui.dp(this, 24), 0, Ui.dp(this, 24), 0); options.addView(weekly);
                AppGraph.IO.execute(() -> {
                    try {
                        String conflict = AppGraph.actions(this).findCalendarConflict(id, day.getTimeInMillis());
                        runOnUiThread(() -> new AlertDialog.Builder(this).setTitle("План готовки")
                                .setMessage("Приготовить: " + recipe.title + "\n" +
                                        android.text.format.DateFormat.format("d MMMM yyyy, HH:mm", day) +
                                        "\nНапоминание за 1 час." +
                                        (conflict == null ? "" : "\n\nНа это время уже есть событие: «" + conflict + "»."))
                                .setView(options).setNegativeButton("Отмена", null)
                                .setPositiveButton("Сохранить", (dialog, which) -> schedule(day.getTimeInMillis(), weekly.isChecked())).show());
                    } catch (Exception e) { runOnUiThread(() -> Toast.makeText(this, "Календарь: " + e.getMessage(), Toast.LENGTH_LONG).show()); }
                });
            }, day.get(Calendar.HOUR_OF_DAY), day.get(Calendar.MINUTE), true).show();
        }, day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void schedule(long time, boolean weekly) {
        calendarButton.setEnabled(false);
        AppGraph.IO.execute(() -> {
            try {
                AppGraph.actions(this).schedule(id, time, weekly);
                runOnUiThread(() -> { Toast.makeText(this, "Событие создано", Toast.LENGTH_SHORT).show(); onResume(); });
            } catch (Exception e) { runOnUiThread(() -> { calendarButton.setEnabled(true); Toast.makeText(this, "Календарь: " + e.getMessage(), Toast.LENGTH_LONG).show(); }); }
        });
    }
    private void deleteRecipe() {
        AppGraph.IO.execute(() -> {
            try { AppGraph.actions(this).delete(id); runOnUiThread(this::finish); }
            catch (Exception e) { runOnUiThread(() -> Toast.makeText(this, "Не удалось удалить: " + e.getMessage(), Toast.LENGTH_LONG).show()); }
        });
    }
}
