package com.example.lab2.data.datasource;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lab2.data.model.AppointmentDraftDto;

public class SharedPreferencesDraftDataSource {
    private static final String PREFS_NAME = "clinic_appointment_draft_v2";

    private static final String KEY_FIO = "fio";
    private static final String KEY_DOB_STRING = "dob_string";
    private static final String KEY_DOB_MS = "dob_ms";
    private static final String KEY_OMS = "oms";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_SPECIALTY = "specialty";
    private static final String KEY_DATE_STRING = "date_string";
    private static final String KEY_DATE_MS = "date_ms";
    private static final String KEY_TIME_STRING = "time_string";
    private static final String KEY_TIME_HOUR = "time_hour";
    private static final String KEY_TIME_MINUTE = "time_minute";
    private static final String KEY_COMPLAINTS = "complaints";
    private static final String KEY_IS_COMMERCIAL = "is_commercial";
    private static final String KEY_PROMO = "promo";
    private static final String KEY_REMINDER_OPT = "reminder_opt";
    private static final String KEY_REMINDER_MIN = "reminder_min";

    private final SharedPreferences prefs;

    public SharedPreferencesDraftDataSource(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveDraft(AppointmentDraftDto dto) {
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(KEY_FIO, dto.fio);
        editor.putString(KEY_DOB_STRING, dto.dobString);
        if (dto.dobMs != null) editor.putLong(KEY_DOB_MS, dto.dobMs);
        else editor.remove(KEY_DOB_MS);

        editor.putString(KEY_OMS, dto.omsPolicy);
        editor.putString(KEY_PHONE, dto.phone);
        editor.putString(KEY_SPECIALTY, dto.specialty);

        editor.putString(KEY_DATE_STRING, dto.appointmentDateString);
        if (dto.appointmentDateMs != null) editor.putLong(KEY_DATE_MS, dto.appointmentDateMs);
        else editor.remove(KEY_DATE_MS);

        editor.putString(KEY_TIME_STRING, dto.appointmentTimeString);
        if (dto.appointmentHour != null) editor.putInt(KEY_TIME_HOUR, dto.appointmentHour);
        else editor.remove(KEY_TIME_HOUR);
        if (dto.appointmentMinute != null) editor.putInt(KEY_TIME_MINUTE, dto.appointmentMinute);
        else editor.remove(KEY_TIME_MINUTE);

        editor.putString(KEY_COMPLAINTS, dto.complaints);
        editor.putBoolean(KEY_IS_COMMERCIAL, dto.isCommercial);
        editor.putString(KEY_PROMO, dto.promoCode);
        editor.putString(KEY_REMINDER_OPT, dto.reminderOption);
        editor.putInt(KEY_REMINDER_MIN, dto.reminderMinutes);

        editor.apply();
    }

    public AppointmentDraftDto getDraft() {
        AppointmentDraftDto dto = new AppointmentDraftDto();
        dto.fio = prefs.getString(KEY_FIO, "");
        dto.dobString = prefs.getString(KEY_DOB_STRING, "");
        if (prefs.contains(KEY_DOB_MS)) dto.dobMs = prefs.getLong(KEY_DOB_MS, 0);

        dto.omsPolicy = prefs.getString(KEY_OMS, "");
        dto.phone = prefs.getString(KEY_PHONE, "");
        dto.specialty = prefs.getString(KEY_SPECIALTY, "");

        dto.appointmentDateString = prefs.getString(KEY_DATE_STRING, "");
        if (prefs.contains(KEY_DATE_MS)) dto.appointmentDateMs = prefs.getLong(KEY_DATE_MS, 0);

        dto.appointmentTimeString = prefs.getString(KEY_TIME_STRING, "");
        if (prefs.contains(KEY_TIME_HOUR)) dto.appointmentHour = prefs.getInt(KEY_TIME_HOUR, 0);
        if (prefs.contains(KEY_TIME_MINUTE)) dto.appointmentMinute = prefs.getInt(KEY_TIME_MINUTE, 0);

        dto.complaints = prefs.getString(KEY_COMPLAINTS, "");
        dto.isCommercial = prefs.getBoolean(KEY_IS_COMMERCIAL, false);
        dto.promoCode = prefs.getString(KEY_PROMO, "");
        dto.reminderOption = prefs.getString(KEY_REMINDER_OPT, "");
        dto.reminderMinutes = prefs.getInt(KEY_REMINDER_MIN, 0);

        return dto;
    }

    public boolean hasDraft() {
        return !prefs.getString(KEY_FIO, "").isEmpty() ||
               !prefs.getString(KEY_OMS, "").isEmpty() ||
               !prefs.getString(KEY_PHONE, "").isEmpty() ||
               !prefs.getString(KEY_SPECIALTY, "").isEmpty() ||
               !prefs.getString(KEY_COMPLAINTS, "").isEmpty();
    }

    public void clearDraft() {
        prefs.edit().clear().apply();
    }
}
