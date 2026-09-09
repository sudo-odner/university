package com.example.lab1.model;

import java.io.Serializable;
import java.util.Arrays;

public class PriceBreakdown implements Serializable {
    private final float baseFare;
    private final float distanceFare;
    private final float durationFare;
    private final float optionsFare;
    private final float passengerMultiplier;
    private final float demandMultiplier;
    private final float subtotal;
    private final float totalPrice;

    public PriceBreakdown(float baseFare, float distanceFare, float durationFare,
                          float optionsFare, float passengerMultiplier, float demandMultiplier) {
        this.baseFare = baseFare;
        this.distanceFare = distanceFare;
        this.durationFare = durationFare;
        this.optionsFare = optionsFare;
        this.passengerMultiplier = passengerMultiplier;
        this.demandMultiplier = demandMultiplier;
        this.subtotal = (baseFare + distanceFare + durationFare + optionsFare) * passengerMultiplier;
        this.totalPrice = Math.round(subtotal * demandMultiplier);
    }

    public float getBaseFare() {
        return baseFare;
    }

    public float getDistanceFare() {
        return distanceFare;
    }

    public float getDurationFare() {
        return durationFare;
    }

    public float getOptionsFare() {
        return optionsFare;
    }

    public float getPassengerMultiplier() {
        return passengerMultiplier;
    }

    public float getDemandMultiplier() {
        return demandMultiplier;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public float getTotalPrice() {
        return totalPrice;
    }

    public float[] getProportions() {
        float sum = baseFare + distanceFare + durationFare + optionsFare;
        if (sum <= 0f) {
            return new float[]{0.25f, 0.25f, 0.25f, 0.25f};
        }
        return new float[]{
                baseFare / sum,
                distanceFare / sum,
                durationFare / sum,
                optionsFare / sum
        };
    }

    @Override
    public String toString() {
        return "PriceBreakdown{" +
                "baseFare=" + baseFare +
                ", distanceFare=" + distanceFare +
                ", durationFare=" + durationFare +
                ", optionsFare=" + optionsFare +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
