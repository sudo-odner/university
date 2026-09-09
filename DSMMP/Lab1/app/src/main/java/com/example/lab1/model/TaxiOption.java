package com.example.lab1.model;

import androidx.annotation.NonNull;

public enum TaxiOption {
    CHILD_SEAT("Детское кресло", 150),
    BAGGAGE("Крупный багаж", 200),
    PETS("Перевозить питомца", 150),
    EXPRESS("Экспресс-подача", 250),
    SILENT_DRIVER("Тихий водитель", 50);

    private final String title;
    private final int price;

    TaxiOption(String title, int price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }

    @NonNull
    @Override
    public String toString() {
        return title + " (+" + price + " ₽)";
    }
}
