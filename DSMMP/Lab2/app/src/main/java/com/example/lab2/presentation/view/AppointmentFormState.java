package com.example.lab2.presentation.view;

import com.example.lab2.domain.model.PriceResult;

public class AppointmentFormState {
    public String fioError = null;
    public String dobError = null;
    public String omsError = null;
    public String phoneError = null;
    public String specialtyError = null;
    public String dateError = null;
    public String timeError = null;
    public String complaintsError = null;
    public String promoError = null;
    public String promoHelper = null;

    public PriceResult priceResult = new PriceResult(0, 0, 0, false, false, true);
    public boolean isFormValid = false;
    public String firstInvalidFieldName = null;
}
