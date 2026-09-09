package com.example.lab1.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TaxiOrderState implements Serializable {
    private final TaxiClass taxiClass;
    private final TariffZone zone;
    private final Set<TaxiOption> options;
    private final int passengerCount;
    private final float demandMultiplier;

    public TaxiOrderState(TaxiClass taxiClass, TariffZone zone, Set<TaxiOption> options,
                          int passengerCount, float demandMultiplier) {
        this.taxiClass = taxiClass;
        this.zone = zone;
        this.options = Collections.unmodifiableSet(new HashSet<>(options));
        this.passengerCount = passengerCount;
        this.demandMultiplier = demandMultiplier;
    }

    public static TaxiOrderState initial() {
        return new TaxiOrderState(
                TaxiClass.ECONOMY,
                TariffZone.CITY,
                Collections.emptySet(),
                1,
                1.0f
        );
    }

    public TaxiClass getTaxiClass() {
        return taxiClass;
    }

    public TariffZone getZone() {
        return zone;
    }

    public Set<TaxiOption> getOptions() {
        return options;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public float getDemandMultiplier() {
        return demandMultiplier;
    }

    public TaxiOrderState withTaxiClass(TaxiClass newClass) {
        return new TaxiOrderState(newClass, this.zone, this.options, this.passengerCount, this.demandMultiplier);
    }

    public TaxiOrderState withZone(TariffZone newZone) {
        return new TaxiOrderState(this.taxiClass, newZone, this.options, this.passengerCount, this.demandMultiplier);
    }

    public TaxiOrderState withOptionToggled(TaxiOption option) {
        Set<TaxiOption> newOptions = new HashSet<>(this.options);
        if (newOptions.contains(option)) {
            newOptions.remove(option);
        } else {
            newOptions.add(option);
        }
        return new TaxiOrderState(this.taxiClass, this.zone, newOptions, this.passengerCount, this.demandMultiplier);
    }

    public TaxiOrderState withPassengerCount(int count) {
        int validCount = Math.max(1, Math.min(6, count));
        return new TaxiOrderState(this.taxiClass, this.zone, this.options, validCount, this.demandMultiplier);
    }

    public TaxiOrderState withDemandMultiplier(float multiplier) {
        return new TaxiOrderState(this.taxiClass, this.zone, this.options, this.passengerCount, multiplier);
    }
}
