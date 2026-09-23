package com.example.lab2.presentation.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;

import com.example.lab2.R;
import com.example.lab2.domain.model.Appointment;
import com.example.lab2.domain.model.Patient;
import com.example.lab2.domain.model.PriceResult;
import com.example.lab2.domain.model.ValidationResult;
import com.example.lab2.presentation.helper.CalendarIntentHelper;
import com.example.lab2.presentation.helper.PermissionHelper;
import com.example.lab2.presentation.viewmodel.MainViewModel;
import com.example.lab2.presentation.viewmodel.MainViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    // Views
    private NestedScrollView nestedScrollView;
    private TextInputLayout tilFio, tilDob, tilOms, tilPhone;
    private TextInputLayout tilSpecialty, tilDate, tilTime, tilComplaints, tilPromo, tilReminder;

    private TextInputEditText etFio, etDob, etOms, etPhone;
    private AutoCompleteTextView actvSpecialty;
    private TextInputEditText etDate, etTime, etComplaints, etPromo;
    private AutoCompleteTextView actvReminder;

    private RadioGroup rgPaymentType;
    private MaterialRadioButton rbOms, rbCommercial;
    private LinearLayout layoutPromo;
    private TextView tvPriceSummary;
    private MaterialButton btnSubmit, btnClear;

    // State
    private Long birthDateMs = null;
    private Long appointmentDateMs = null;
    private Integer appointmentHour = null;
    private Integer appointmentMinute = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private boolean isRestoringDraft = false;

    // Runtime Permission Launcher for Android 13+
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    showSnackbar(getString(R.string.msg_permission_granted));
                } else {
                    // Graceful degradation: inform user that form remains fully functional
                    showSnackbar(getString(R.string.msg_permission_denied));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this, new MainViewModelFactory(this))
                .get(MainViewModel.class);

        initViews();
        setupDropdowns();
        setupPickers();
        setupPaymentAndPromo();
        setupInlineValidation();
        setupButtons();

        // Restore draft from persistent storage
        restoreDraft();

        // Check notification permission with pre-explanation
        PermissionHelper.checkNotificationPermission(
                this,
                notificationPermissionLauncher,
                () -> showSnackbar(getString(R.string.msg_permission_denied))
        );
    }

    private void initViews() {
        nestedScrollView = findViewById(R.id.nestedScrollView);

        tilFio = findViewById(R.id.tilFio);
        tilDob = findViewById(R.id.tilDob);
        tilOms = findViewById(R.id.tilOms);
        tilPhone = findViewById(R.id.tilPhone);
        tilSpecialty = findViewById(R.id.tilSpecialty);
        tilDate = findViewById(R.id.tilDate);
        tilTime = findViewById(R.id.tilTime);
        tilComplaints = findViewById(R.id.tilComplaints);
        tilPromo = findViewById(R.id.tilPromo);
        tilReminder = findViewById(R.id.tilReminder);

        etFio = findViewById(R.id.etFio);
        etDob = findViewById(R.id.etDob);
        etOms = findViewById(R.id.etOms);
        etPhone = findViewById(R.id.etPhone);
        actvSpecialty = findViewById(R.id.actvSpecialty);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etComplaints = findViewById(R.id.etComplaints);
        etPromo = findViewById(R.id.etPromo);
        actvReminder = findViewById(R.id.actvReminder);

        rgPaymentType = findViewById(R.id.rgPaymentType);
        rbOms = findViewById(R.id.rbOms);
        rbCommercial = findViewById(R.id.rbCommercial);
        layoutPromo = findViewById(R.id.layoutPromo);
        tvPriceSummary = findViewById(R.id.tvPriceSummary);

        btnSubmit = findViewById(R.id.btnSubmit);
        btnClear = findViewById(R.id.btnClear);
    }

    private void setupDropdowns() {
        // Specialties
        String[] specialties = getResources().getStringArray(R.array.specialties_array);
        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, specialties);
        actvSpecialty.setAdapter(specialtyAdapter);

        // Reminder options
        String[] reminders = getResources().getStringArray(R.array.reminder_array);
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, reminders);
        actvReminder.setAdapter(reminderAdapter);
        actvReminder.setText(reminders[0], false); // Default: "За 1 час"
    }

    private void setupPickers() {
        // 1. Date of Birth Picker (past dates only)
        etDob.setOnClickListener(v -> showBirthDatePicker());
        tilDob.setEndIconOnClickListener(v -> showBirthDatePicker());

        // 2. Desired Appointment Date Picker (future dates only, starting today)
        etDate.setOnClickListener(v -> showAppointmentDatePicker());
        tilDate.setEndIconOnClickListener(v -> showAppointmentDatePicker());

        // 3. Desired Appointment Time Picker (clinic hours: 08:00 - 20:00)
        etTime.setOnClickListener(v -> showAppointmentTimePicker());
        tilTime.setEndIconOnClickListener(v -> showAppointmentTimePicker());
    }

    private void showBirthDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();

        long initialSelection = birthDateMs != null ? birthDateMs : MaterialDatePicker.todayInUtcMilliseconds();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.dob_label))
                .setSelection(initialSelection)
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                birthDateMs = selection;
                Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                utcCalendar.setTimeInMillis(selection);
                etDob.setText(dateFormat.format(utcCalendar.getTime()));
                tilDob.setError(null);
                saveCurrentDraft();
            }
        });

        picker.show(getSupportFragmentManager(), "DOB_PICKER");
    }

    private void showAppointmentDatePicker() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();

        long initialSelection = appointmentDateMs != null ? appointmentDateMs : MaterialDatePicker.todayInUtcMilliseconds();

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_label))
                .setSelection(initialSelection)
                .setCalendarConstraints(constraints)
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                appointmentDateMs = selection;
                Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                utcCalendar.setTimeInMillis(selection);
                etDate.setText(dateFormat.format(utcCalendar.getTime()));
                tilDate.setError(null);
                saveCurrentDraft();
            }
        });

        picker.show(getSupportFragmentManager(), "APPOINTMENT_DATE_PICKER");
    }

    private void showAppointmentTimePicker() {
        int initialHour = appointmentHour != null ? appointmentHour : 10;
        int initialMinute = appointmentMinute != null ? appointmentMinute : 0;

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(initialHour)
                .setMinute(initialMinute)
                .setTitleText(getString(R.string.time_label))
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();

            ValidationResult timeValidation = viewModel.validateTime(
                    String.format(Locale.getDefault(), "%02d:%02d", hour, minute), hour, minute);

            if (!timeValidation.isValid()) {
                tilTime.setError(timeValidation.getErrorMessage());
                return;
            }

            appointmentHour = hour;
            appointmentMinute = minute;
            etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            tilTime.setError(null);
            saveCurrentDraft();
        });

        picker.show(getSupportFragmentManager(), "APPOINTMENT_TIME_PICKER");
    }

    private void setupPaymentAndPromo() {
        rgPaymentType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCommercial) {
                layoutPromo.setVisibility(View.VISIBLE);
            } else {
                layoutPromo.setVisibility(View.GONE);
                tilPromo.setError(null);
            }
            updatePriceCalculation();
            saveCurrentDraft();
        });

        etPromo.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePriceCalculation();
                saveCurrentDraft();
            }
        });
    }

    private void updatePriceCalculation() {
        boolean isCommercial = rbCommercial.isChecked();
        String promo = etPromo.getText() != null ? etPromo.getText().toString().trim() : "";
        PriceResult price = viewModel.calculatePrice(isCommercial, promo);

        if (!isCommercial) {
            tvPriceSummary.setText(getString(R.string.price_total_oms));
            tvPriceSummary.setTextColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            if (!promo.isEmpty()) {
                if (price.isPromoApplied()) {
                    tilPromo.setHelperText(getString(R.string.promo_applied));
                    tilPromo.setError(null);
                    tvPriceSummary.setText(String.format(Locale.getDefault(),
                            "Итого к оплате: %d ₽ (скидка 20%%)", price.getFinalPrice()));
                    tvPriceSummary.setTextColor(ContextCompat.getColor(this, R.color.success_green));
                } else {
                    tilPromo.setHelperText(null);
                    tilPromo.setError(getString(R.string.promo_invalid));
                    tvPriceSummary.setText(String.format(Locale.getDefault(),
                            getString(R.string.price_total_commercial), price.getFinalPrice()));
                    tvPriceSummary.setTextColor(ContextCompat.getColor(this, R.color.black));
                }
            } else {
                tilPromo.setHelperText(getString(R.string.promo_helper));
                tilPromo.setError(null);
                tvPriceSummary.setText(String.format(Locale.getDefault(),
                        getString(R.string.price_total_commercial), price.getFinalPrice()));
                tvPriceSummary.setTextColor(ContextCompat.getColor(this, R.color.black));
            }
        }
    }

    private void setupInlineValidation() {
        // FIO: error on focus lost; remove error on text change
        etFio.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                ValidationResult res = viewModel.validateFio(getText(etFio));
                if (!res.isValid()) tilFio.setError(res.getErrorMessage());
            }
        });
        etFio.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilFio.getError() != null && viewModel.validateFio(s.toString()).isValid()) {
                    tilFio.setError(null);
                }
                saveCurrentDraft();
            }
        });

        // OMS: 16 digits
        etOms.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                ValidationResult res = viewModel.validateOms(getText(etOms));
                if (!res.isValid()) tilOms.setError(res.getErrorMessage());
            }
        });
        etOms.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilOms.getError() != null && viewModel.validateOms(s.toString()).isValid()) {
                    tilOms.setError(null);
                }
                saveCurrentDraft();
            }
        });

        // Phone
        etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                ValidationResult res = viewModel.validatePhone(getText(etPhone));
                if (!res.isValid()) tilPhone.setError(res.getErrorMessage());
            }
        });
        etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilPhone.getError() != null && viewModel.validatePhone(s.toString()).isValid()) {
                    tilPhone.setError(null);
                }
                saveCurrentDraft();
            }
        });

        // Complaints
        etComplaints.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                ValidationResult res = viewModel.validateComplaints(getText(etComplaints));
                if (!res.isValid()) tilComplaints.setError(res.getErrorMessage());
            }
        });
        etComplaints.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tilComplaints.getError() != null && viewModel.validateComplaints(s.toString()).isValid()) {
                    tilComplaints.setError(null);
                }
                saveCurrentDraft();
            }
        });

        // Specialty dropdown selection
        actvSpecialty.setOnItemClickListener((parent, view, position, id) -> {
            tilSpecialty.setError(null);
            saveCurrentDraft();
        });

        actvReminder.setOnItemClickListener((parent, view, position, id) -> saveCurrentDraft());
    }

    private void setupButtons() {
        btnSubmit.setOnClickListener(v -> {
            AppointmentFormState state = viewModel.validateAll(
                    getText(etFio), getText(etDob), birthDateMs,
                    getText(etOms), getText(etPhone), getText(actvSpecialty),
                    getText(etDate), appointmentDateMs,
                    getText(etTime), appointmentHour, appointmentMinute,
                    getText(etComplaints), rbCommercial.isChecked(), getText(etPromo)
            );

            applyValidationState(state);

            if (state.isFormValid) {
                showConfirmationDialog();
            }
        });

        btnClear.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("Очистить форму?")
                .setMessage("Все заполненные поля и сохранённый черновик будут сброшены.")
                .setPositiveButton("Очистить", (dialog, which) -> clearFormAndDraft())
                .setNegativeButton("Отмена", null)
                .show());
    }

    private void applyValidationState(AppointmentFormState state) {
        tilFio.setError(state.fioError);
        tilDob.setError(state.dobError);
        tilOms.setError(state.omsError);
        tilPhone.setError(state.phoneError);
        tilSpecialty.setError(state.specialtyError);
        tilDate.setError(state.dateError);
        tilTime.setError(state.timeError);
        tilComplaints.setError(state.complaintsError);

        if (!state.isFormValid) {
            View target = null;
            if ("fio".equals(state.firstInvalidFieldName)) target = tilFio;
            else if ("dob".equals(state.firstInvalidFieldName)) target = tilDob;
            else if ("oms".equals(state.firstInvalidFieldName)) target = tilOms;
            else if ("phone".equals(state.firstInvalidFieldName)) target = tilPhone;
            else if ("specialty".equals(state.firstInvalidFieldName)) target = tilSpecialty;
            else if ("date".equals(state.firstInvalidFieldName)) target = tilDate;
            else if ("time".equals(state.firstInvalidFieldName)) target = tilTime;
            else if ("complaints".equals(state.firstInvalidFieldName)) target = tilComplaints;

            if (target != null) {
                final View scrollTarget = target;
                nestedScrollView.post(() -> nestedScrollView.smoothScrollTo(0, scrollTarget.getTop()));
            }
            showSnackbar(getString(R.string.msg_form_has_errors));
        }
    }

    private Appointment buildCurrentAppointment() {
        Patient patient = new Patient(
                getText(etFio),
                getText(etDob),
                birthDateMs,
                getText(etOms),
                getText(etPhone)
        );

        boolean isCommercial = rbCommercial.isChecked();
        String promo = getText(etPromo);
        PriceResult price = viewModel.calculatePrice(isCommercial, promo);
        String reminderOption = getText(actvReminder);
        int reminderMin = viewModel.parseReminderMinutes(reminderOption);

        return new Appointment(
                patient,
                getText(actvSpecialty),
                getText(etDate),
                appointmentDateMs,
                getText(etTime),
                appointmentHour,
                appointmentMinute,
                getText(etComplaints),
                isCommercial,
                promo,
                price,
                reminderOption,
                reminderMin
        );
    }

    private void showConfirmationDialog() {
        Appointment appointment = buildCurrentAppointment();

        String payment = appointment.isCommercial() ?
                String.format(Locale.getDefault(), "Платный приём (%d ₽)", appointment.getPriceResult().getFinalPrice()) :
                "По ОМС (0 ₽)";

        String message = String.format(Locale.getDefault(),
                "Пациент: %s\n" +
                "Дата рождения: %s\n" +
                "Полис ОМС: %s\n" +
                "Телефон: %s\n\n" +
                "Врач: %s\n" +
                "Дата и время: %s в %s\n" +
                "Оплата: %s\n" +
                "Напоминание: %s",
                appointment.getPatient().getFio(),
                appointment.getPatient().getDobString(),
                appointment.getPatient().getOmsPolicy(),
                appointment.getPatient().getPhone(),
                appointment.getDoctorSpecialty(),
                appointment.getAppointmentDateString(),
                appointment.getAppointmentTimeString(),
                payment,
                appointment.getReminderOption());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_confirm_title)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_confirm_positive, (dialog, which) -> onAppointmentConfirmed(appointment))
                .setNegativeButton(R.string.dialog_confirm_negative, null)
                .show();
    }

    private void onAppointmentConfirmed(Appointment appointment) {
        viewModel.clearDraft();

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_success_title)
                .setMessage(R.string.dialog_success_message)
                .setPositiveButton(R.string.dialog_success_calendar, (dialog, which) -> {
                    Intent intent = CalendarIntentHelper.createCalendarIntent(this, appointment);
                    if (intent != null && CalendarIntentHelper.canResolveIntent(this, intent)) {
                        startActivity(intent);
                    } else {
                        showSnackbar(getString(R.string.msg_no_calendar_app));
                    }
                })
                .setNegativeButton(R.string.dialog_success_close, null)
                .setCancelable(false)
                .show();
    }

    private void saveCurrentDraft() {
        if (isRestoringDraft) return;
        viewModel.saveDraft(buildCurrentAppointment());
    }

    private void restoreDraft() {
        isRestoringDraft = true;
        if (viewModel.hasDraft()) {
            Appointment draft = viewModel.getDraft();
            if (draft != null) {
                if (draft.getPatient() != null) {
                    etFio.setText(draft.getPatient().getFio());
                    if (draft.getPatient().getDobString() != null) {
                        etDob.setText(draft.getPatient().getDobString());
                        birthDateMs = draft.getPatient().getDobMs();
                    }
                    etOms.setText(draft.getPatient().getOmsPolicy());
                    etPhone.setText(draft.getPatient().getPhone());
                }

                if (draft.getDoctorSpecialty() != null && !draft.getDoctorSpecialty().isEmpty()) {
                    actvSpecialty.setText(draft.getDoctorSpecialty(), false);
                }

                if (draft.getAppointmentDateString() != null) {
                    etDate.setText(draft.getAppointmentDateString());
                    appointmentDateMs = draft.getAppointmentDateMs();
                }

                if (draft.getAppointmentTimeString() != null) {
                    etTime.setText(draft.getAppointmentTimeString());
                    appointmentHour = draft.getAppointmentHour();
                    appointmentMinute = draft.getAppointmentMinute();
                }

                etComplaints.setText(draft.getComplaints());

                if (draft.isCommercial()) {
                    rbCommercial.setChecked(true);
                    layoutPromo.setVisibility(View.VISIBLE);
                    etPromo.setText(draft.getPromoCode());
                } else {
                    rbOms.setChecked(true);
                    layoutPromo.setVisibility(View.GONE);
                }

                if (draft.getReminderOption() != null && !draft.getReminderOption().isEmpty()) {
                    actvReminder.setText(draft.getReminderOption(), false);
                }

                updatePriceCalculation();
                showSnackbar(getString(R.string.msg_draft_restored));
            }
        }
        isRestoringDraft = false;
    }

    private void clearFormAndDraft() {
        viewModel.clearDraft();
        isRestoringDraft = true;

        etFio.setText("");
        etDob.setText("");
        birthDateMs = null;
        etOms.setText("");
        etPhone.setText("");
        actvSpecialty.setText("", false);
        etDate.setText("");
        appointmentDateMs = null;
        etTime.setText("");
        appointmentHour = null;
        appointmentMinute = null;
        etComplaints.setText("");
        etPromo.setText("");
        rbOms.setChecked(true);
        layoutPromo.setVisibility(View.GONE);

        tilFio.setError(null);
        tilDob.setError(null);
        tilOms.setError(null);
        tilPhone.setError(null);
        tilSpecialty.setError(null);
        tilDate.setError(null);
        tilTime.setError(null);
        tilComplaints.setError(null);
        tilPromo.setError(null);

        updatePriceCalculation();
        isRestoringDraft = false;

        showSnackbar(getString(R.string.msg_draft_cleared));
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentDraft();
    }

    private String getText(TextView view) {
        return view.getText() != null ? view.getText().toString().trim() : "";
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(R.id.coordinatorLayout), message, Snackbar.LENGTH_LONG).show();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
