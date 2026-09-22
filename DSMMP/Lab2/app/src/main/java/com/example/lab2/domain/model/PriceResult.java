package com.example.lab2.domain.model;

public class PriceResult {
    private final int finalPrice;
    private final int basePrice;
    private final int discount;
    private final boolean isPromoApplied;
    private final boolean isPromoValid;
    private final boolean isOms;

    public PriceResult(int finalPrice, int basePrice, int discount, boolean isPromoApplied, boolean isPromoValid, boolean isOms) {
        this.finalPrice = finalPrice;
        this.basePrice = basePrice;
        this.discount = discount;
        this.isPromoApplied = isPromoApplied;
        this.isPromoValid = isPromoValid;
        this.isOms = isOms;
    }

    public int getFinalPrice() { return finalPrice; }
    public int getBasePrice() { return basePrice; }
    public int getDiscount() { return discount; }
    public boolean isPromoApplied() { return isPromoApplied; }
    public boolean isPromoValid() { return isPromoValid; }
    public boolean isOms() { return isOms; }
}
