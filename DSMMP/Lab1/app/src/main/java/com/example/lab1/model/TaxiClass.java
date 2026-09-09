package com.example.lab1.model;

import androidx.annotation.NonNull;

public enum TaxiClass {
    ECONOMY("Эконом", 150, 20, 5),
    COMFORT("Комфорт", 250, 30, 8),
    BUSINESS("Бизнес", 450, 50, 15),
    MINIVAN("Минивэн", 350, 40, 12);

    private final String title;
    private final int baseFare;
    private final int perKmRate;
    private final int perMinRate;

    TaxiClass(String title, int baseFare, int perKmRate, int perMinRate) {
        this.title = title;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.perMinRate = perMinRate;
    }

    public String getTitle() {
        return title;
    }

    public int getBaseFare() {
        return baseFare;
    }

    public int getPerKmRate() {
        return perKmRate;
    }

    public int getPerMinRate() {
        return perMinRate;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
