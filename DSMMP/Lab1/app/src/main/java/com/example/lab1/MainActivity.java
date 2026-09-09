package com.example.lab1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.lab1.model.PriceBreakdown;
import com.example.lab1.model.TariffZone;
import com.example.lab1.model.TaxiClass;
import com.example.lab1.model.TaxiOption;
import com.example.lab1.model.TaxiOrderState;
import com.example.lab1.view.TaxiPriceBreakdownView;
import com.example.lab1.viewmodel.TaxiViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private TaxiViewModel viewModel;

    private ChipGroup chipGroupClass;
    private ChipGroup chipGroupZone;
    private ChipGroup chipGroupOptions;
    private Chip chipOptChildSeat, chipOptBaggage, chipOptPets, chipOptExpress, chipOptSilent;
    private TextView tvPassengerCount;
    private Button btnDecrementPassengers, btnIncrementPassengers;
    private ChipGroup chipGroupDemand;
    private TaxiPriceBreakdownView priceBreakdownView;
    private TextView tvTotalPrice;
    private Button btnProceed;
    private View rootContainer;

    private boolean isUpdatingUiFromState = false;

    private final ActivityResultLauncher<Intent> confirmLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    int orderId = result.getData().getIntExtra(ConfirmActivity.EXTRA_ORDER_ID, -1);
                    viewModel.onOrderConfirmed(orderId);
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    viewModel.onOrderCanceled();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View coordinatorRoot = findViewById(R.id.coordinatorRoot);
        if (coordinatorRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(coordinatorRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
                return insets;
            });
        }

        viewModel = new ViewModelProvider(this).get(TaxiViewModel.class);

        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        rootContainer = findViewById(R.id.rootContainer);
        chipGroupClass = findViewById(R.id.chipGroupClass);
        chipGroupZone = findViewById(R.id.chipGroupZone);
        chipGroupOptions = findViewById(R.id.chipGroupOptions);

        chipOptChildSeat = findViewById(R.id.chipOptChildSeat);
        chipOptBaggage = findViewById(R.id.chipOptBaggage);
        chipOptPets = findViewById(R.id.chipOptPets);
        chipOptExpress = findViewById(R.id.chipOptExpress);
        chipOptSilent = findViewById(R.id.chipOptSilent);

        tvPassengerCount = findViewById(R.id.tvPassengerCount);
        btnDecrementPassengers = findViewById(R.id.btnDecrementPassengers);
        btnIncrementPassengers = findViewById(R.id.btnIncrementPassengers);

        chipGroupDemand = findViewById(R.id.chipGroupDemand);
        priceBreakdownView = findViewById(R.id.priceBreakdownView);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnProceed = findViewById(R.id.btnProceed);
    }

    private void setupListeners() {
        chipGroupClass.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (isUpdatingUiFromState || checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipClassEconomy) {
                viewModel.setTaxiClass(TaxiClass.ECONOMY);
            } else if (checkedId == R.id.chipClassComfort) {
                viewModel.setTaxiClass(TaxiClass.COMFORT);
            } else if (checkedId == R.id.chipClassBusiness) {
                viewModel.setTaxiClass(TaxiClass.BUSINESS);
            } else if (checkedId == R.id.chipClassMinivan) {
                viewModel.setTaxiClass(TaxiClass.MINIVAN);
            }
        });

        chipGroupZone.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (isUpdatingUiFromState || checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipZoneCity) {
                viewModel.setZone(TariffZone.CITY);
            } else if (checkedId == R.id.chipZoneSuburb) {
                viewModel.setZone(TariffZone.SUBURB);
            } else if (checkedId == R.id.chipZoneAirport) {
                viewModel.setZone(TariffZone.AIRPORT);
            }
        });

        chipOptChildSeat.setOnClickListener(v -> viewModel.toggleOption(TaxiOption.CHILD_SEAT));
        chipOptBaggage.setOnClickListener(v -> viewModel.toggleOption(TaxiOption.BAGGAGE));
        chipOptPets.setOnClickListener(v -> viewModel.toggleOption(TaxiOption.PETS));
        chipOptExpress.setOnClickListener(v -> viewModel.toggleOption(TaxiOption.EXPRESS));
        chipOptSilent.setOnClickListener(v -> viewModel.toggleOption(TaxiOption.SILENT_DRIVER));

        btnDecrementPassengers.setOnClickListener(v -> viewModel.decrementPassengers());
        btnIncrementPassengers.setOnClickListener(v -> viewModel.incrementPassengers());

        chipGroupDemand.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (isUpdatingUiFromState || checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipDemandNormal) {
                viewModel.setDemandMultiplier(1.0f);
            } else if (checkedId == R.id.chipDemandHigh) {
                viewModel.setDemandMultiplier(1.3f);
            } else if (checkedId == R.id.chipDemandPeak) {
                viewModel.setDemandMultiplier(1.8f);
            }
        });

        btnProceed.setOnClickListener(v -> {
            TaxiOrderState currentState = viewModel.getState().getValue();
            PriceBreakdown currentBreakdown = viewModel.getPriceBreakdown().getValue();
            if (currentState != null && currentBreakdown != null) {
                Intent intent = new Intent(MainActivity.this, ConfirmActivity.class);
                intent.putExtra(ConfirmActivity.EXTRA_ORDER_STATE, currentState);
                intent.putExtra(ConfirmActivity.EXTRA_PRICE_BREAKDOWN, currentBreakdown);
                confirmLauncher.launch(intent);
            }
        });
    }

    private void observeViewModel() {
        viewModel.getState().observe(this, this::renderState);
        viewModel.getPriceBreakdown().observe(this, this::renderPriceBreakdown);
        viewModel.getOrderEvent().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(rootContainer != null ? rootContainer : findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void renderState(TaxiOrderState state) {
        if (state == null) return;
        isUpdatingUiFromState = true;

        switch (state.getTaxiClass()) {
            case ECONOMY:
                chipGroupClass.check(R.id.chipClassEconomy);
                break;
            case COMFORT:
                chipGroupClass.check(R.id.chipClassComfort);
                break;
            case BUSINESS:
                chipGroupClass.check(R.id.chipClassBusiness);
                break;
            case MINIVAN:
                chipGroupClass.check(R.id.chipClassMinivan);
                break;
        }

        switch (state.getZone()) {
            case CITY:
                chipGroupZone.check(R.id.chipZoneCity);
                break;
            case SUBURB:
                chipGroupZone.check(R.id.chipZoneSuburb);
                break;
            case AIRPORT:
                chipGroupZone.check(R.id.chipZoneAirport);
                break;
        }

        chipOptChildSeat.setChecked(state.getOptions().contains(TaxiOption.CHILD_SEAT));
        chipOptBaggage.setChecked(state.getOptions().contains(TaxiOption.BAGGAGE));
        chipOptPets.setChecked(state.getOptions().contains(TaxiOption.PETS));
        chipOptExpress.setChecked(state.getOptions().contains(TaxiOption.EXPRESS));
        chipOptSilent.setChecked(state.getOptions().contains(TaxiOption.SILENT_DRIVER));

        int count = state.getPassengerCount();
        String passengerText;
        if (count == 1) {
            passengerText = "1 пассажир";
        } else if (count >= 2 && count <= 4) {
            passengerText = count + " пассажира";
        } else {
            passengerText = count + " пассажиров";
        }
        tvPassengerCount.setText(passengerText);

        btnDecrementPassengers.setEnabled(count > 1);
        btnIncrementPassengers.setEnabled(count < 6);

        float mult = state.getDemandMultiplier();
        if (Math.abs(mult - 1.0f) < 0.05f) {
            chipGroupDemand.check(R.id.chipDemandNormal);
        } else if (Math.abs(mult - 1.3f) < 0.05f) {
            chipGroupDemand.check(R.id.chipDemandHigh);
        } else if (Math.abs(mult - 1.8f) < 0.05f) {
            chipGroupDemand.check(R.id.chipDemandPeak);
        }

        isUpdatingUiFromState = false;
    }

    private void renderPriceBreakdown(PriceBreakdown breakdown) {
        if (breakdown == null) return;
        priceBreakdownView.setBreakdown(breakdown);
        tvTotalPrice.setText(Math.round(breakdown.getTotalPrice()) + " ₽");
    }
}
