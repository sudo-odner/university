package com.example.lab4.data.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.example.lab4.domain.repository.PhotoRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

public class PhotoStore implements PhotoRepository {
    private final Context context;
    public PhotoStore(Context context) { this.context = context.getApplicationContext(); }

    @Override public String importPhoto(Uri source) throws Exception {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream stream = context.getContentResolver().openInputStream(source)) {
            if (stream == null) throw new IllegalArgumentException("Не удалось открыть фотографию");
            BitmapFactory.decodeStream(stream, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw new IllegalArgumentException("Неверный формат изображения");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight);
        Bitmap decoded;
        try (InputStream stream = context.getContentResolver().openInputStream(source)) {
            if (stream == null) throw new IllegalArgumentException("Не удалось открыть фотографию");
            decoded = BitmapFactory.decodeStream(stream, null, options);
        }
        if (decoded == null) throw new IllegalArgumentException("Не удалось декодировать фотографию");
        Bitmap scaled = decoded;
        int max = Math.max(decoded.getWidth(), decoded.getHeight());
        if (max > 1024) {
            float factor = 1024f / max;
            scaled = Bitmap.createScaledBitmap(decoded, Math.round(decoded.getWidth() * factor),
                    Math.round(decoded.getHeight() * factor), true);
            decoded.recycle();
        }
        File directory = new File(context.getFilesDir(), "images");
        if (!directory.exists() && !directory.mkdirs()) throw new IllegalStateException("Не удалось создать папку изображений");
        File output = new File(directory, UUID.randomUUID() + ".jpg");
        try {
            int quality = 80;
            while (true) {
                try (FileOutputStream stream = new FileOutputStream(output, false)) {
                    if (!scaled.compress(Bitmap.CompressFormat.JPEG, quality, stream))
                        throw new IllegalStateException("Не удалось сохранить фотографию");
                }
                if (output.length() <= 500_000 || scaled.getWidth() <= 256 && scaled.getHeight() <= 256) break;
                if (quality > 60) { quality -= 10; continue; }
                Bitmap smaller = Bitmap.createScaledBitmap(scaled,
                        Math.max(1, Math.round(scaled.getWidth() * .75f)),
                        Math.max(1, Math.round(scaled.getHeight() * .75f)), true);
                scaled.recycle(); scaled = smaller; quality = 80;
            }
        } catch (Exception e) {
            output.delete(); throw e;
        } finally { scaled.recycle(); }
        return output.getAbsolutePath();
    }
    static int calculateSampleSize(int width, int height) {
        int sampleSize = 1;
        int longest = Math.max(width, height);
        while (longest / sampleSize > 2048) sampleSize *= 2;
        return sampleSize;
    }
    @Override public void delete(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.getParentFile() != null && file.getParentFile().equals(new File(context.getFilesDir(), "images"))) file.delete();
        }
    }
}
