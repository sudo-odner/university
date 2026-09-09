package com.example.lab1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab1.model.PriceBreakdown;
import com.example.lab1.model.TaxiOption;
import com.example.lab1.model.TaxiOrderState;
import com.example.lab1.view.TaxiPriceBreakdownView;

import java.util.Random;

public class ConfirmActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_STATE = "extra_order_state";
    public static final String EXTRA_PRICE_BREAKDOWN = "extra_price_breakdown";
    public static final String EXTRA_ORDER_ID = "extra_order_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        View confirmCoordinatorRoot = findViewById(R.id.confirmCoordinatorRoot);
        if (confirmCoordinatorRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(confirmCoordinatorRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        TextView tvSummaryClass = findViewById(R.id.tvSummaryClass);
        TextView tvSummaryZone = findViewById(R.id.tvSummaryZone);
        TextView tvSummaryPassengers = findViewById(R.id.tvSummaryPassengers);
        TextView tvSummaryDemand = findViewById(R.id.tvSummaryDemand);
        TextView tvSummaryOptions = findViewById(R.id.tvSummaryOptions);
        TextView tvConfirmTotalPrice = findViewById(R.id.tvConfirmTotalPrice);
        TaxiPriceBreakdownView priceBreakdownView = findViewById(R.id.confirmPriceBreakdownView);

        Button btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        Button btnCancelOrder = findViewById(R.id.btnCancelOrder);

        Intent intent = getIntent();
        TaxiOrderState state = (TaxiOrderState) intent.getSerializableExtra(EXTRA_ORDER_STATE);
        PriceBreakdown breakdown = (PriceBreakdown) intent.getSerializableExtra(EXTRA_PRICE_BREAKDOWN);

        if (state != null && breakdown != null) {
            tvSummaryClass.setText("Класс: " + state.getTaxiClass().getTitle() +
                    " (Подача " + state.getTaxiClass().getBaseFare() + " ₽, " +
                    state.getTaxiClass().getPerKmRate() + " ₽/км)");
            tvSummaryZone.setText("Тарифная зона: " + state.getZone().getTitle() +
                    " (~" + state.getZone().getEstimatedKm() + " км, " +
                    state.getZone().getEstimatedMinutes() + " мин)");
            tvSummaryPassengers.setText("Число пассажиров: " + state.getPassengerCount() + " чел.");
            tvSummaryDemand.setText("Спрос: " + String.format("%.1fx", state.getDemandMultiplier()));

            if (state.getOptions().isEmpty()) {
                tvSummaryOptions.setText("Дополнительные опции: Нет");
            } else {
                StringBuilder sb = new StringBuilder("Опции: ");
                for (TaxiOption option : state.getOptions()) {
                    sb.append(option.getTitle()).append(", ");
                }
                if (sb.length() > 2) {
                    sb.setLength(sb.length() - 2);
                }
                tvSummaryOptions.setText(sb.toString());
            }

            priceBreakdownView.setBreakdown(breakdown);
            tvConfirmTotalPrice.setText(Math.round(breakdown.getTotalPrice()) + " ₽");
        }

        btnConfirmOrder.setOnClickListener(v -> {
            int randomOrderId = 10000 + new Random().nextInt(90000);
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_ORDER_ID, randomOrderId);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnCancelOrder.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
