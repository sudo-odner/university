package com.example.lab2.presentation.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.lab2.domain.model.Appointment;
import com.example.lab2.domain.model.Patient;
import com.example.lab2.domain.model.PriceResult;
import com.example.lab2.domain.model.ValidationResult;
import com.example.lab2.domain.usecase.CalculatePriceUseCase;
import com.example.lab2.domain.usecase.ClearDraftUseCase;
import com.example.lab2.domain.usecase.GetDraftUseCase;
import com.example.lab2.domain.usecase.SaveDraftUseCase;
import com.example.lab2.domain.usecase.ValidateComplaintsUseCase;
import com.example.lab2.domain.usecase.ValidateDateUseCase;
import com.example.lab2.domain.usecase.ValidateDobUseCase;
import com.example.lab2.domain.usecase.ValidateFioUseCase;
import com.example.lab2.domain.usecase.ValidateOmsUseCase;
import com.example.lab2.domain.usecase.ValidatePhoneUseCase;
import com.example.lab2.domain.usecase.ValidateSpecialtyUseCase;
import com.example.lab2.domain.usecase.ValidateTimeUseCase;
import com.example.lab2.presentation.view.AppointmentFormState;

public class MainViewModel extends ViewModel {

    private final ValidateFioUseCase validateFioUseCase;
    private final ValidateDobUseCase validateDobUseCase;
    private final ValidateOmsUseCase validateOmsUseCase;
    private final ValidatePhoneUseCase validatePhoneUseCase;
    private final ValidateSpecialtyUseCase validateSpecialtyUseCase;
    private final ValidateDateUseCase validateDateUseCase;
    private final ValidateTimeUseCase validateTimeUseCase;
    private final ValidateComplaintsUseCase validateComplaintsUseCase;
    private final CalculatePriceUseCase calculatePriceUseCase;
    private final SaveDraftUseCase saveDraftUseCase;
    private final GetDraftUseCase getDraftUseCase;
    private final ClearDraftUseCase clearDraftUseCase;

    public MainViewModel(
            ValidateFioUseCase validateFioUseCase,
            ValidateDobUseCase validateDobUseCase,
            ValidateOmsUseCase validateOmsUseCase,
            ValidatePhoneUseCase validatePhoneUseCase,
            ValidateSpecialtyUseCase validateSpecialtyUseCase,
            ValidateDateUseCase validateDateUseCase,
            ValidateTimeUseCase validateTimeUseCase,
            ValidateComplaintsUseCase validateComplaintsUseCase,
            CalculatePriceUseCase calculatePriceUseCase,
            SaveDraftUseCase saveDraftUseCase,
            GetDraftUseCase getDraftUseCase,
            ClearDraftUseCase clearDraftUseCase) {
        this.validateFioUseCase = validateFioUseCase;
        this.validateDobUseCase = validateDobUseCase;
        this.validateOmsUseCase = validateOmsUseCase;
        this.validatePhoneUseCase = validatePhoneUseCase;
        this.validateSpecialtyUseCase = validateSpecialtyUseCase;
        this.validateDateUseCase = validateDateUseCase;
        this.validateTimeUseCase = validateTimeUseCase;
        this.validateComplaintsUseCase = validateComplaintsUseCase;
        this.calculatePriceUseCase = calculatePriceUseCase;
        this.saveDraftUseCase = saveDraftUseCase;
        this.getDraftUseCase = getDraftUseCase;
        this.clearDraftUseCase = clearDraftUseCase;
    }

    public ValidationResult validateFio(String fio) {
        return validateFioUseCase.execute(fio);
    }

    public ValidationResult validateDob(String dobString, Long dobMs) {
        return validateDobUseCase.execute(dobString, dobMs);
    }

    public ValidationResult validateOms(String oms) {
        return validateOmsUseCase.execute(oms);
    }

    public ValidationResult validatePhone(String phone) {
        return validatePhoneUseCase.execute(phone);
    }

    public ValidationResult validateSpecialty(String specialty) {
        return validateSpecialtyUseCase.execute(specialty);
    }

    public ValidationResult validateDate(String dateString, Long dateMs) {
        return validateDateUseCase.execute(dateString, dateMs);
    }

    public ValidationResult validateTime(String timeString, Integer hour, Integer minute) {
        return validateTimeUseCase.execute(timeString, hour, minute);
    }

    public ValidationResult validateComplaints(String complaints) {
        return validateComplaintsUseCase.execute(complaints);
    }

    public PriceResult calculatePrice(boolean isCommercial, String promoCode) {
        return calculatePriceUseCase.execute(isCommercial, promoCode);
    }

    public AppointmentFormState validateAll(String fio, String dobString, Long dobMs,
                                            String oms, String phone, String specialty,
                                            String dateString, Long dateMs,
                                            String timeString, Integer hour, Integer minute,
                                            String complaints, boolean isCommercial, String promoCode) {
        AppointmentFormState state = new AppointmentFormState();
        boolean valid = true;

        ValidationResult fioRes = validateFio(fio);
        if (!fioRes.isValid()) {
            valid = false;
            state.fioError = fioRes.getErrorMessage();
            if (state.firstInvalidFieldName == null) state.firstInvalidFieldName = "fio";
        }

        ValidationResult dobRes = validateDob(dobString, dobMs);
        if (!dobRes.isValid()) {
            valid = false;
            state.dobError = dobRes.getErrorMessage();
            if (state.firstInvalidFieldName == null) state.firstInvalidFieldName = "dob";
        }

        ValidationResult omsRes = validateOms(oms);
        if (!omsRes.isValid()) {
            valid = false;
            state.omsError = omsRes.getErrorMessage();
            if (state.firstInvalidFieldName == null) state.firstInvalidFieldName = "oms";
        }

        ValidationResult phoneRes = validatePhone(phone);
        if (!phoneRes.isValid()) {
            valid = false;
            state.phoneError = phoneRes.getErrorMessage();
            if (state.firstInvalidFieldName == null) state.firstInvalidFieldName = "phone";
        }

        ValidationResult specRes = validateSpecialty(specialty);
        if (!specRes.isValid()) {
            valid = false;
            state.specialtyError = specRes.getErrorMessage();
            if (state.firstInvalidFieldName == null) state.firstInvalidFieldName = "specialty";
        }

        ValidationResult dateRes = validateDate(dateString, dateMs);
        if (!dateRes.isValid()) {
            valid = false;
            state.dateError = dateRes.getErrorMessage();
            if (state.firstInvalidFieldName == null) state.firstInvalidFieldName = "date";
        }

        ValidationResult timeRes = validateTime(timeString, hour, minute);
        if (!timeRes.isValid()) {
            valid = false;
            state.timeError = timeRes.getErrorMessage();
            if (state.firstInvalidFieldName == null) state.firstInvalidFieldName = "time";
        }

        ValidationResult compRes = validateComplaints(complaints);
        if (!compRes.isValid()) {
            valid = false;
            state.complaintsError = compRes.getErrorMessage();
            if (state.firstInvalidFieldName == null) state.firstInvalidFieldName = "complaints";
        }

        state.priceResult = calculatePrice(isCommercial, promoCode);
        state.isFormValid = valid;
        return state;
    }

    public void saveDraft(Appointment appointment) {
        saveDraftUseCase.execute(appointment);
    }

    public Appointment getDraft() {
        return getDraftUseCase.execute();
    }

    public boolean hasDraft() {
        return getDraftUseCase.hasDraft();
    }

    public void clearDraft() {
        clearDraftUseCase.execute();
    }

    public int parseReminderMinutes(String reminderOption) {
        if (reminderOption == null) return 0;
        if (reminderOption.contains("1 час")) return 60;
        if (reminderOption.contains("2 часа")) return 120;
        if (reminderOption.contains("1 день")) return 1440;
        return 0;
    }
}
