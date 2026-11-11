package com.example.lab4.presentation.editor;

import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import com.example.lab4.data.media.PhotoStore;
import com.example.lab4.domain.model.Recipe;
import com.example.lab4.presentation.common.AppGraph;
import com.example.lab4.presentation.common.Ui;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;

public class EditorActivity extends AppCompatActivity {
    private Uri selected, cameraUri;
    private EditText title, category, minutes, ingredients, steps;
    private TextInputLayout titleBox, categoryBox, minutesBox, ingredientsBox, stepsBox;
    private ImageView preview;
    private TextView photoHint;
    private ProgressBar progress;
    private MaterialButton save;
    private ActivityResultLauncher<PickVisualMediaRequest> picker;
    private ActivityResultLauncher<Uri> camera;
    private File cameraFile;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Ui.CREAM); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        picker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> { if (uri != null) choose(uri); });
        camera = registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
            if (ok && cameraUri != null) choose(cameraUri); else cleanupCamera();
        });
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); LinearLayout root = Ui.column(this); scroll.addView(root);
        TextView back = Ui.muted(this, "‹  Все рецепты", 16); back.setMinHeight(Ui.dp(this, 48)); back.setGravity(Gravity.CENTER_VERTICAL); root.addView(back); back.setOnClickListener(v -> finish());
        Ui.space(root, 20); root.addView(Ui.text(this, "Новый рецепт", 30, true));
        Ui.space(root, 6); root.addView(Ui.muted(this, "Запишите всё, чтобы повторить блюдо в любой момент.", 15));
        Ui.space(root, 26);
        root.addView(Ui.text(this, "Фотография блюда", 20, true)); Ui.space(root, 10);
        LinearLayout photoCard = new LinearLayout(this); photoCard.setOrientation(LinearLayout.VERTICAL); photoCard.setGravity(Gravity.CENTER);
        photoCard.setBackground(Ui.rounded(this, Ui.SAND, 20)); photoCard.setClipToOutline(true);
        preview = new ImageView(this); preview.setScaleType(ImageView.ScaleType.CENTER_CROP); preview.setVisibility(View.GONE);
        photoCard.addView(preview, new LinearLayout.LayoutParams(-1, Ui.dp(this, 210)));
        photoHint = Ui.muted(this, "✦\nДобавьте снимок готового блюда", 16); photoHint.setGravity(Gravity.CENTER);
        photoCard.addView(photoHint, new LinearLayout.LayoutParams(-1, Ui.dp(this, 210))); root.addView(photoCard);
        Ui.space(root, 10);
        LinearLayout photoActions = new LinearLayout(this); root.addView(photoActions);
        MaterialButton gallery = Ui.button(this, "Из галереи"); photoActions.addView(gallery, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams cameraParams = new LinearLayout.LayoutParams(0, -2, 1); cameraParams.leftMargin=Ui.dp(this, 8);
        MaterialButton take = Ui.button(this, "Снять фото"); photoActions.addView(take, cameraParams);
        gallery.setOnClickListener(v -> picker.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        take.setOnClickListener(v -> {
            try { cameraFile = File.createTempFile("camera_", ".jpg", getCacheDir());
                cameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cameraFile); camera.launch(cameraUri);
            } catch (Exception e) { Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show(); }
        });
        Ui.space(root, 28); root.addView(Ui.text(this, "Основное", 20, true)); Ui.space(root, 12);
        titleBox = field(root, "Название блюда", "Например, паста с томатами", false); title = titleBox.getEditText();
        categoryBox = field(root, "Категория", "Завтрак, обед, ужин…", false); category = categoryBox.getEditText();
        minutesBox = field(root, "Время приготовления", "Минуты", false); minutes = minutesBox.getEditText(); minutes.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        Ui.space(root, 18); root.addView(Ui.text(this, "Как приготовить", 20, true)); Ui.space(root, 12);
        ingredientsBox = field(root, "Ингредиенты", "Каждый ингредиент с новой строки", true); ingredients = ingredientsBox.getEditText();
        stepsBox = field(root, "Пошаговое описание", "Опишите последовательность действий", true); steps = stepsBox.getEditText();
        Ui.space(root, 20);
        progress = new ProgressBar(this); progress.setVisibility(View.GONE); LinearLayout.LayoutParams spinner = new LinearLayout.LayoutParams(Ui.dp(this, 36), Ui.dp(this, 36)); spinner.gravity=Gravity.CENTER; root.addView(progress, spinner);
        save = Ui.button(this, "Сохранить рецепт"); root.addView(save, new LinearLayout.LayoutParams(-1, Ui.dp(this, 56))); save.setOnClickListener(v -> save());
        Ui.space(root, 8); TextView note = Ui.muted(this, "Фото сохранится только на этом устройстве.", 13); note.setGravity(Gravity.CENTER); root.addView(note);
        setContentView(scroll);
    }
    private TextInputLayout field(LinearLayout root, String label, String hint, boolean multiline) {
        TextView caption = Ui.text(this, label, 15, true); root.addView(caption); Ui.space(root, 7);
        TextInputLayout box = new TextInputLayout(this); box.setHintEnabled(false);
        EditText input = new com.google.android.material.textfield.TextInputEditText(this);
        input.setTextSize(16); input.setHint(hint); input.setSingleLine(!multiline);
        input.setBackground(Ui.rounded(this, 0xffffffff, 14));
        input.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        if (multiline) { input.setMinLines(4); input.setGravity(Gravity.TOP); }
        else input.setMinHeight(Ui.dp(this, 54));
        box.addView(input); LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin=Ui.dp(this, 18); root.addView(box, params); return box;
    }
    private void choose(Uri uri) {
        selected = uri; photoHint.setVisibility(View.GONE); preview.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).centerCrop().into(preview);
    }
    private void cleanupCamera() { if (cameraFile != null) cameraFile.delete(); cameraFile = null; }
    private boolean valid() {
        titleBox.setError(null); categoryBox.setError(null); minutesBox.setError(null); ingredientsBox.setError(null); stepsBox.setError(null);
        boolean ok = true;
        if (title.getText().toString().trim().isEmpty()) { titleBox.setError("Укажите название"); ok=false; }
        if (category.getText().toString().trim().isEmpty()) { categoryBox.setError("Укажите категорию"); ok=false; }
        try { if (Integer.parseInt(minutes.getText().toString().trim()) <= 0) throw new NumberFormatException(); }
        catch (Exception e) { minutesBox.setError("Введите число больше нуля"); ok=false; }
        if (ingredients.getText().toString().trim().isEmpty()) { ingredientsBox.setError("Добавьте ингредиенты"); ok=false; }
        if (steps.getText().toString().trim().isEmpty()) { stepsBox.setError("Опишите приготовление"); ok=false; }
        if (selected == null) { photoHint.setText("Нужно добавить фотографию блюда"); Toast.makeText(this, "Добавьте фотографию", Toast.LENGTH_SHORT).show(); ok=false; }
        return ok;
    }
    private void save() {
        if (!valid()) return;
        String t=title.getText().toString().trim(), c=category.getText().toString().trim();
        String i=ingredients.getText().toString().trim(), s=steps.getText().toString().trim();
        int duration=Integer.parseInt(minutes.getText().toString().trim()); Uri photo=selected;
        progress.setVisibility(View.VISIBLE); save.setText("Сохраняем фото…"); save.setEnabled(false);
        AppGraph.IO.execute(() -> {
            String path=null;
            try { PhotoStore store=new PhotoStore(this); path=store.importPhoto(photo);
                AppGraph.actions(this).add(new Recipe(0,t,c,duration,i,s,path,null));
                runOnUiThread(() -> { cleanupCamera(); finish(); });
            } catch (Exception e) { if (path != null) new PhotoStore(this).delete(path);
                runOnUiThread(() -> { progress.setVisibility(View.GONE); save.setText("Сохранить рецепт"); save.setEnabled(true);
                    Toast.makeText(this, "Ошибка сохранения: " + e.getMessage(), Toast.LENGTH_LONG).show(); }); }
        });
    }
}
