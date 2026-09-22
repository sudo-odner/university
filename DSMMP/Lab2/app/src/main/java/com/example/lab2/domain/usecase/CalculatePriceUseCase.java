package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.PriceResult;
import java.util.Locale;

public class CalculatePriceUseCase {
    public static final int BASE_PRICE = 2500;
    public static final int DISCOUNT = 500;

    public PriceResult execute(boolean isCommercial, String promoCode) {
        if (!isCommercial) {
            return new PriceResult(0, 0, 0, false, false, true);
        }

        if (promoCode == null || promoCode.trim().isEmpty()) {
            return new PriceResult(BASE_PRICE, BASE_PRICE, 0, false, false, false);
        }

        String clean = promoCode.trim().toUpperCase(Locale.ROOT);
        boolean isValid = clean.equals("ЗДОРОВЬЕ") || clean.equals("ВЕСНА") ||
                clean.equals("МЕД2024") || clean.equals("HEALTH") ||
                clean.equals("PROMO20");

        if (isValid) {
            return new PriceResult(BASE_PRICE - DISCOUNT, BASE_PRICE, DISCOUNT, true, true, false);
        } else {
            return new PriceResult(BASE_PRICE, BASE_PRICE, 0, false, false, false);
        }
    }
}
