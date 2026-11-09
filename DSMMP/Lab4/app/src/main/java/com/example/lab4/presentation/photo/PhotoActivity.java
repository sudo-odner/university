package com.example.lab4.presentation.photo;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.io.File;

public class PhotoActivity extends AppCompatActivity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        String path = getIntent().getStringExtra("path");
        if (path == null || !new File(path).exists()) { finish(); return; }
        ZoomImageView image = new ZoomImageView(this);
        image.setBackgroundColor(Color.BLACK);
        setContentView(image);
        Glide.with(this).load(new File(path)).into(image);
    }
}
