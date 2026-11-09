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
        ScrollView scroll = new ScrollView(this); LinearLayout root = Ui.column(this); scroll.addView(root);
        root.addView(Ui.text(this, recipe.title, 30, true));
        root.addView(Ui.text(this, recipe.category + " · " + recipe.minutes + " мин", 16, false));
        ImageView image = new ImageView(this); image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(image, new LinearLayout.LayoutParams(-1, Ui.dp(this, 260)));
        Glide.with(this).load(new File(recipe.imagePath)).override(900, 900).into(image);
        image.setOnClickListener(v -> startActivity(new Intent(this, PhotoActivity.class).putExtra("path", recipe.imagePath)));
        root.addView(Ui.text(this, "Ингредиенты", 22, true)); root.addView(Ui.text(this, recipe.ingredients, 17, false));
        root.addView(Ui.text(this, "Приготовление", 22, true)); root.addView(Ui.text(this, recipe.steps, 17, false));
        calendarButton = Ui.button(this, recipe.calendarEventId == null ? "Запланировать готовку" : "Изменить дату готовки");
        root.addView(calendarButton); calendarButton.setOnClickListener(v -> {
            if (hasCalendarPermission()) pickDate(); else permissionRequest.launch(new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR});
        });
        Button delete = Ui.button(this, "Удалить рецепт"); root.addView(delete);
        delete.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Удалить рецепт?")
                .setMessage("Фотография и связанное событие календаря тоже будут удалены.")
                .setNegativeButton("Отмена", null).setPositiveButton("Удалить", (d, w) -> deleteRecipe()).show());
        setContentView(scroll);
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
                CheckBox weekly = new CheckBox(this); weekly.setText("Повторять каждую неделю");
                new AlertDialog.Builder(this).setTitle("План готовки")
                        .setMessage("Событие: «Приготовить: " + recipe.title + "». Напоминание за 1 час.")
                        .setView(weekly).setNegativeButton("Отмена", null)
                        .setPositiveButton("Создать", (dialog, which) -> schedule(day.getTimeInMillis(), weekly.isChecked())).show();
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
