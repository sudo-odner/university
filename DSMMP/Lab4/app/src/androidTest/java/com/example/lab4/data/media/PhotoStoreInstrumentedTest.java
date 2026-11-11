package com.example.lab4.data.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PhotoStoreInstrumentedTest {
    @Test public void importsAndCompressesPhoto() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        File source = new File(context.getCacheDir(), "photo_test_source.jpg");
        Bitmap bitmap = Bitmap.createBitmap(3000, 2000, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xff8a633e);
        try (FileOutputStream stream = new FileOutputStream(source)) {
            assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream));
        }
        bitmap.recycle();
        PhotoStore store = new PhotoStore(context);
        String path = store.importPhoto(Uri.fromFile(source));
        try {
            File saved = new File(path);
            assertTrue(saved.exists());
            assertTrue(saved.length() > 0 && saved.length() <= 500_000);
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true; BitmapFactory.decodeFile(path, bounds);
            assertTrue(Math.max(bounds.outWidth, bounds.outHeight) <= 1024);
        } finally { store.delete(path); source.delete(); }
    }
}
