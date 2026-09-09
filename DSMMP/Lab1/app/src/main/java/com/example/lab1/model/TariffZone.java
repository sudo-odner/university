package com.example.lab1.model;

import androidx.annotation.NonNull;

public enum TariffZone {
    CITY("Городская зона", 10, 15, 1.0f),
    SUBURB("Загородная зона", 25, 30, 1.25f),
    AIRPORT("Аэропорт", 40, 45, 1.5f);

    private final String title;
    private final int estimatedKm;
    private final int estimatedMinutes;
    private final float zoneMultiplier;

    TariffZone(String title, int estimatedKm, int estimatedMinutes, float zoneMultiplier) {
        this.title = title;
        this.estimatedKm = estimatedKm;
        this.estimatedMinutes = estimatedMinutes;
        this.zoneMultiplier = zoneMultiplier;
    }

    public String getTitle() {
        return title;
    }

    public int getEstimatedKm() {
        return estimatedKm;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public float getZoneMultiplier() {
        return zoneMultiplier;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }
}
