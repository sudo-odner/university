package com.example.lab4.presentation.common;

import android.content.Context;
import com.example.lab4.data.calendar.AndroidCalendarRepository;
import com.example.lab4.data.local.RecipeDatabase;
import com.example.lab4.data.media.PhotoStore;
import com.example.lab4.domain.usecase.RecipeActions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppGraph {
    private static RecipeActions actions;
    public static final ExecutorService IO = Executors.newSingleThreadExecutor();
    public static synchronized RecipeActions actions(Context context) {
        if (actions == null) actions = new RecipeActions(new RecipeDatabase(context.getApplicationContext()),
                new PhotoStore(context), new AndroidCalendarRepository(context));
        return actions;
    }
    private AppGraph() { }
}
