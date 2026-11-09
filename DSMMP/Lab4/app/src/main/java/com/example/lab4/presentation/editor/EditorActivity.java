package com.example.lab4.presentation.editor;

import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.lab4.data.media.PhotoStore;
import com.example.lab4.domain.model.Recipe;
import com.example.lab4.presentation.common.AppGraph;
import com.example.lab4.presentation.common.Ui;
import java.io.File;

public class EditorActivity extends AppCompatActivity {
    private Uri selected, cameraUri;
    private EditText title, category, minutes, ingredients, steps;
    private ImageView preview;
    private ProgressBar progress;
    private Button save;
    private ActivityResultLauncher<PickVisualMediaRequest> picker;
    private ActivityResultLauncher<Uri> camera;
    private File cameraFile;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        picker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> { if (uri != null) choose(uri); });
        camera = registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
            if (ok && cameraUri != null) choose(cameraUri); else cleanupCamera();
        });
        ScrollView scroll = new ScrollView(this); LinearLayout root = Ui.column(this); scroll.addView(root);
        root.addView(Ui.text(this, "Новый рецепт", 28, true));
        title = field(root, "Название блюда", false); category = field(root, "Категория", false);
        minutes = field(root, "Время приготовления, минуты", false); minutes.setInputType(2);
        ingredients = field(root, "Ингредиенты (каждый с новой строки)", true);
        steps = field(root, "Приготовление по шагам", true);
        preview = new ImageView(this); preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        root.addView(preview, new LinearLayout.LayoutParams(-1, Ui.dp(this, 220)));
        Button gallery = Ui.button(this, "Выбрать фото"); root.addView(gallery);
        gallery.setOnClickListener(v -> picker.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        Button take = Ui.button(this, "Снять фото"); root.addView(take);
        take.setOnClickListener(v -> {
            try {
                cameraFile = File.createTempFile("camera_", ".jpg", getCacheDir());
                cameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", cameraFile);
                camera.launch(cameraUri);
            } catch (Exception e) { Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show(); }
        });
        progress = new ProgressBar(this); progress.setVisibility(android.view.View.GONE); root.addView(progress);
        save = Ui.button(this, "Сохранить рецепт"); root.addView(save); save.setOnClickListener(v -> save());
        setContentView(scroll);
    }
    private EditText field(LinearLayout root, String hint, boolean multiline) {
        EditText f = new EditText(this); f.setHint(hint);
        if (multiline) { f.setMinLines(3); f.setGravity(android.view.Gravity.TOP); }
        root.addView(f, new LinearLayout.LayoutParams(-1, -2)); return f;
    }
    private void choose(Uri uri) { selected = uri; preview.setImageURI(uri); }
    private void cleanupCamera() { if (cameraFile != null) cameraFile.delete(); cameraFile = null; }
    private void save() {
        String t = title.getText().toString().trim(), c = category.getText().toString().trim();
        String i = ingredients.getText().toString().trim(), s = steps.getText().toString().trim();
        int m;
        try { m = Integer.parseInt(minutes.getText().toString().trim()); } catch (Exception e) { m = 0; }
        if (t.isEmpty() || c.isEmpty() || i.isEmpty() || s.isEmpty() || m <= 0 || selected == null) {
            Toast.makeText(this, "Заполните все поля и добавьте фото", Toast.LENGTH_LONG).show(); return;
        }
        Uri photo = selected; int duration = m;
        progress.setVisibility(android.view.View.VISIBLE); save.setEnabled(false);
        AppGraph.IO.execute(() -> {
            String path = null;
            try {
                PhotoStore store = new PhotoStore(this);
                path = store.importPhoto(photo);
                AppGraph.actions(this).add(new Recipe(0, t, c, duration, i, s, path, null));
                runOnUiThread(() -> { cleanupCamera(); finish(); });
            } catch (Exception e) {
                if (path != null) new PhotoStore(this).delete(path);
                runOnUiThread(() -> { progress.setVisibility(android.view.View.GONE); save.setEnabled(true);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show(); });
            }
        });
    }
}
