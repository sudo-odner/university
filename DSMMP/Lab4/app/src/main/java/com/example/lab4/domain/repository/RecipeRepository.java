package com.example.lab4.domain.repository;

import com.example.lab4.domain.model.Recipe;
import java.util.List;

public interface RecipeRepository {
    List<Recipe> all();
    Recipe get(long id);
    long add(Recipe recipe);
    void setCalendarEvent(long recipeId, Long eventId);
    void delete(long id);
}
