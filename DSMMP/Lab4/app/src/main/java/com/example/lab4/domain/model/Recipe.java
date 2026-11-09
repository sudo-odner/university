package com.example.lab4.domain.model;

public class Recipe {
    public long id;
    public String title;
    public String category;
    public int minutes;
    public String ingredients;
    public String steps;
    public String imagePath;
    public Long calendarEventId;

    public Recipe(long id, String title, String category, int minutes, String ingredients,
                  String steps, String imagePath, Long calendarEventId) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.minutes = minutes;
        this.ingredients = ingredients;
        this.steps = steps;
        this.imagePath = imagePath;
        this.calendarEventId = calendarEventId;
    }
}
