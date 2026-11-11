package com.example.lab4.domain.usecase;

import com.example.lab4.domain.model.Recipe;
import com.example.lab4.domain.repository.CalendarRepository;
import com.example.lab4.domain.repository.PhotoRepository;
import com.example.lab4.domain.repository.RecipeRepository;
import org.junit.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class RecipeActionsTest {
    static class Recipes implements RecipeRepository {
        Recipe item = new Recipe(1, "Суп", "Обед", 30, "Вода", "Сварить", "/photo.jpg", 7L);
        @Override public List<Recipe> all() { return Collections.singletonList(item); }
        @Override public Recipe get(long id) { return item; }
        @Override public long add(Recipe recipe) { item = recipe; return 1; }
        @Override public void setCalendarEvent(long id, Long eventId) { item.calendarEventId = eventId; }
        @Override public void delete(long id) { item = null; }
    }
    static class Photos implements PhotoRepository {
        String deleted;
        @Override public String importPhoto(android.net.Uri uri) { return "/photo.jpg"; }
        @Override public void delete(String path) { deleted = path; }
    }
    static class Calendar implements CalendarRepository {
        Long deleted;
        @Override public String findConflict(long starts, Long excluded) { return null; }
        @Override public long schedule(String title, long starts, boolean weekly) { return 9L; }
        @Override public void delete(long id) { deleted = id; }
    }
    @Test public void deletingRecipeCleansCalendarAndPhoto() throws Exception {
        Recipes recipes = new Recipes(); Photos photos = new Photos(); Calendar calendar = new Calendar();
        new RecipeActions(recipes, photos, calendar).delete(1);
        assertNull(recipes.item); assertEquals("/photo.jpg", photos.deleted); assertEquals(Long.valueOf(7), calendar.deleted);
    }
    @Test public void reschedulingSavesNewEventAndRemovesOld() throws Exception {
        Recipes recipes = new Recipes(); Calendar calendar = new Calendar();
        new RecipeActions(recipes, new Photos(), calendar).schedule(1, 123L, true);
        assertEquals(Long.valueOf(9), recipes.item.calendarEventId);
        assertEquals(Long.valueOf(7), calendar.deleted);
    }
}
