package com.example.lab2.domain.model;

public class Appointment {
    private final Patient patient;
    private final String doctorSpecialty;
    private final String appointmentDateString;
    private final Long appointmentDateMs;
    private final String appointmentTimeString;
    private final Integer appointmentHour;
    private final Integer appointmentMinute;
    private final String complaints;
    private final boolean isCommercial;
    private final String promoCode;
    private final PriceResult priceResult;
    private final String reminderOption;
    private final int reminderMinutes;

    public Appointment(Patient patient, String doctorSpecialty,
                       String appointmentDateString, Long appointmentDateMs,
                       String appointmentTimeString, Integer appointmentHour, Integer appointmentMinute,
                       String complaints, boolean isCommercial, String promoCode,
                       PriceResult priceResult, String reminderOption, int reminderMinutes) {
        this.patient = patient;
        this.doctorSpecialty = doctorSpecialty;
        this.appointmentDateString = appointmentDateString;
        this.appointmentDateMs = appointmentDateMs;
        this.appointmentTimeString = appointmentTimeString;
        this.appointmentHour = appointmentHour;
        this.appointmentMinute = appointmentMinute;
        this.complaints = complaints;
        this.isCommercial = isCommercial;
        this.promoCode = promoCode;
        this.priceResult = priceResult;
        this.reminderOption = reminderOption;
        this.reminderMinutes = reminderMinutes;
    }

    public Patient getPatient() { return patient; }
    public String getDoctorSpecialty() { return doctorSpecialty; }
    public String getAppointmentDateString() { return appointmentDateString; }
    public Long getAppointmentDateMs() { return appointmentDateMs; }
    public String getAppointmentTimeString() { return appointmentTimeString; }
    public Integer getAppointmentHour() { return appointmentHour; }
    public Integer getAppointmentMinute() { return appointmentMinute; }
    public String getComplaints() { return complaints; }
    public boolean isCommercial() { return isCommercial; }
    public String getPromoCode() { return promoCode; }
    public PriceResult getPriceResult() { return priceResult; }
    public String getReminderOption() { return reminderOption; }
    public int getReminderMinutes() { return reminderMinutes; }
}
