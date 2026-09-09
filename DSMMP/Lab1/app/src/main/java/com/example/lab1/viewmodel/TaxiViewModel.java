package com.example.lab1.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lab1.model.PriceBreakdown;
import com.example.lab1.model.TariffZone;
import com.example.lab1.model.TaxiClass;
import com.example.lab1.model.TaxiOption;
import com.example.lab1.model.TaxiOrderState;

public class TaxiViewModel extends ViewModel {

    private final MutableLiveData<TaxiOrderState> state = new MutableLiveData<>(TaxiOrderState.initial());
    private final MutableLiveData<PriceBreakdown> priceBreakdown = new MutableLiveData<>();
    private final MutableLiveData<String> orderEvent = new MutableLiveData<>();

    public TaxiViewModel() {
        recalculate();
    }

    public LiveData<TaxiOrderState> getState() {
        return state;
    }

    public LiveData<PriceBreakdown> getPriceBreakdown() {
        return priceBreakdown;
    }

    public LiveData<String> getOrderEvent() {
        return orderEvent;
    }

    public void setTaxiClass(TaxiClass taxiClass) {
        TaxiOrderState current = getCurrentState();
        if (current.getTaxiClass() != taxiClass) {
            state.setValue(current.withTaxiClass(taxiClass));
            recalculate();
        }
    }

    public void setZone(TariffZone zone) {
        TaxiOrderState current = getCurrentState();
        if (current.getZone() != zone) {
            state.setValue(current.withZone(zone));
            recalculate();
        }
    }

    public void toggleOption(TaxiOption option) {
        TaxiOrderState current = getCurrentState();
        state.setValue(current.withOptionToggled(option));
        recalculate();
    }

    public void setPassengerCount(int count) {
        TaxiOrderState current = getCurrentState();
        int bounded = Math.max(1, Math.min(6, count));
        if (current.getPassengerCount() != bounded) {
            state.setValue(current.withPassengerCount(bounded));
            recalculate();
        }
    }

    public void incrementPassengers() {
        TaxiOrderState current = getCurrentState();
        setPassengerCount(current.getPassengerCount() + 1);
    }

    public void decrementPassengers() {
        TaxiOrderState current = getCurrentState();
        setPassengerCount(current.getPassengerCount() - 1);
    }

    public void setDemandMultiplier(float multiplier) {
        TaxiOrderState current = getCurrentState();
        state.setValue(current.withDemandMultiplier(multiplier));
        recalculate();
    }

    public void onOrderConfirmed(int orderId) {
        orderEvent.setValue("Заказ #" + orderId + " успешно подтверждён!");
    }

    public void onOrderCanceled() {
        orderEvent.setValue("Оформление заказа отменено.");
    }

    // Single recalculation method - single source of truth
    private void recalculate() {
        TaxiOrderState s = getCurrentState();
        TaxiClass tc = s.getTaxiClass();
        TariffZone zone = s.getZone();

        float baseFare = tc.getBaseFare();
        float distanceFare = zone.getEstimatedKm() * tc.getPerKmRate() * zone.getZoneMultiplier();
        float durationFare = zone.getEstimatedMinutes() * tc.getPerMinRate();

        float optionsSum = 0f;
        for (TaxiOption opt : s.getOptions()) {
            optionsSum += opt.getPrice();
        }

        // Each additional passenger above 1 adds a +100 ₽ group passenger surcharge
        float passengerSurcharge = (s.getPassengerCount() - 1) * 100f;
        float totalOptionsFare = optionsSum + passengerSurcharge;

        PriceBreakdown breakdown = new PriceBreakdown(
                baseFare,
                distanceFare,
                durationFare,
                totalOptionsFare,
                1.0f,
                s.getDemandMultiplier()
        );

        priceBreakdown.setValue(breakdown);
    }

    private TaxiOrderState getCurrentState() {
        TaxiOrderState current = state.getValue();
        return current != null ? current : TaxiOrderState.initial();
    }
}
