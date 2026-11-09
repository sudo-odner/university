package com.example.lab4.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.lab4.domain.model.Recipe;
import com.example.lab4.domain.repository.RecipeRepository;
import java.util.ArrayList;
import java.util.List;

public class RecipeDatabase extends SQLiteOpenHelper implements RecipeRepository {
    public RecipeDatabase(Context context) { super(context, "recipes.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, category TEXT NOT NULL, minutes INTEGER NOT NULL, ingredients TEXT NOT NULL, steps TEXT NOT NULL, image_path TEXT NOT NULL, event_id INTEGER)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    private Recipe read(Cursor c) {
        int eventIndex = c.getColumnIndexOrThrow("event_id");
        return new Recipe(c.getLong(c.getColumnIndexOrThrow("id")), c.getString(c.getColumnIndexOrThrow("title")),
                c.getString(c.getColumnIndexOrThrow("category")), c.getInt(c.getColumnIndexOrThrow("minutes")),
                c.getString(c.getColumnIndexOrThrow("ingredients")), c.getString(c.getColumnIndexOrThrow("steps")),
                c.getString(c.getColumnIndexOrThrow("image_path")), c.isNull(eventIndex) ? null : c.getLong(eventIndex));
    }
    @Override public List<Recipe> all() {
        List<Recipe> result = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("recipes", null, null, null, null, null, "id DESC")) {
            while (c.moveToNext()) result.add(read(c));
        }
        return result;
    }
    @Override public Recipe get(long id) {
        try (Cursor c = getReadableDatabase().query("recipes", null, "id=?", new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? read(c) : null;
        }
    }
    @Override public long add(Recipe r) {
        ContentValues v = new ContentValues();
        v.put("title", r.title); v.put("category", r.category); v.put("minutes", r.minutes);
        v.put("ingredients", r.ingredients); v.put("steps", r.steps); v.put("image_path", r.imagePath);
        long id = getWritableDatabase().insertOrThrow("recipes", null, v);
        return id;
    }
    @Override public void setCalendarEvent(long id, Long eventId) {
        ContentValues v = new ContentValues();
        if (eventId == null) v.putNull("event_id"); else v.put("event_id", eventId);
        getWritableDatabase().update("recipes", v, "id=?", new String[]{String.valueOf(id)});
    }
    @Override public void delete(long id) {
        getWritableDatabase().delete("recipes", "id=?", new String[]{String.valueOf(id)});
    }
}
