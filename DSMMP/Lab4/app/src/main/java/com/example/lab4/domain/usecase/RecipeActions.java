package com.example.lab4.domain.usecase;

import com.example.lab4.domain.model.Recipe;
import com.example.lab4.domain.repository.CalendarRepository;
import com.example.lab4.domain.repository.PhotoRepository;
import com.example.lab4.domain.repository.RecipeRepository;
import java.util.List;

public class RecipeActions {
    private final RecipeRepository recipes;
    private final PhotoRepository photos;
    private final CalendarRepository calendar;

    public RecipeActions(RecipeRepository recipes, PhotoRepository photos, CalendarRepository calendar) {
        this.recipes = recipes;
        this.photos = photos;
        this.calendar = calendar;
    }

    public List<Recipe> all() { return recipes.all(); }
    public Recipe get(long id) { return recipes.get(id); }
    public long add(Recipe recipe) { return recipes.add(recipe); }

    public String findCalendarConflict(long id, long startsAtMillis) throws Exception {
        Recipe recipe = recipes.get(id);
        if (recipe == null) throw new IllegalArgumentException("Рецепт не найден");
        return calendar.findConflict(startsAtMillis, recipe.calendarEventId);
    }

    public void schedule(long id, long startsAtMillis, boolean weekly) throws Exception {
        Recipe recipe = recipes.get(id);
        if (recipe == null) throw new IllegalArgumentException("Рецепт не найден");
        Long previousEventId = recipe.calendarEventId;
        long newEvent = calendar.schedule("Приготовить: " + recipe.title, startsAtMillis, weekly);
        try {
            recipes.setCalendarEvent(id, newEvent);
        } catch (Exception e) {
            calendar.delete(newEvent);
            throw e;
        }
        if (previousEventId != null) calendar.delete(previousEventId);
    }

    public void delete(long id) throws Exception {
        Recipe recipe = recipes.get(id);
        if (recipe == null) return;
        if (recipe.calendarEventId != null) calendar.delete(recipe.calendarEventId);
        recipes.delete(id);
        photos.delete(recipe.imagePath);
    }
}
