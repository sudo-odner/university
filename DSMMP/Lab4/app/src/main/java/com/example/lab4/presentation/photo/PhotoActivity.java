package com.example.lab4.presentation.photo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.lab4.presentation.common.Ui;
import com.google.android.material.button.MaterialButton;
import java.io.File;

public class PhotoActivity extends AppCompatActivity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        String path = getIntent().getStringExtra("path");
        if (path == null || !new File(path).exists()) { finish(); return; }
        FrameLayout frame = new FrameLayout(this); frame.setBackgroundColor(Color.BLACK);
        ZoomImageView image = new ZoomImageView(this); frame.addView(image, new FrameLayout.LayoutParams(-1, -1));
        MaterialButton back = new MaterialButton(this); back.setText("‹  Назад"); back.setAllCaps(false);
        back.setTextColor(Color.WHITE); back.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff3b302b));
        back.setCornerRadius(Ui.dp(this, 16));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, Ui.dp(this, 48), Gravity.TOP | Gravity.START);
        params.setMargins(Ui.dp(this, 16), Ui.dp(this, 16), 0, 0); frame.addView(back, params);
        back.setOnClickListener(v -> finish()); setContentView(frame);
        Glide.with(this).load(new File(path)).into(image);
    }
}
