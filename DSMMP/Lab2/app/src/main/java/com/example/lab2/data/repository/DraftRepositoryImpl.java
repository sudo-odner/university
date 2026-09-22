package com.example.lab2.data.repository;

import com.example.lab2.data.datasource.SharedPreferencesDraftDataSource;
import com.example.lab2.data.model.AppointmentDraftDto;
import com.example.lab2.domain.model.Appointment;
import com.example.lab2.domain.model.Patient;
import com.example.lab2.domain.model.PriceResult;
import com.example.lab2.domain.repository.DraftRepository;

public class DraftRepositoryImpl implements DraftRepository {
    private final SharedPreferencesDraftDataSource dataSource;

    public DraftRepositoryImpl(SharedPreferencesDraftDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void saveDraft(Appointment appointment) {
        if (appointment == null) return;
        AppointmentDraftDto dto = new AppointmentDraftDto();
        if (appointment.getPatient() != null) {
            dto.fio = appointment.getPatient().getFio();
            dto.dobString = appointment.getPatient().getDobString();
            dto.dobMs = appointment.getPatient().getDobMs();
            dto.omsPolicy = appointment.getPatient().getOmsPolicy();
            dto.phone = appointment.getPatient().getPhone();
        }
        dto.specialty = appointment.getDoctorSpecialty();
        dto.appointmentDateString = appointment.getAppointmentDateString();
        dto.appointmentDateMs = appointment.getAppointmentDateMs();
        dto.appointmentTimeString = appointment.getAppointmentTimeString();
        dto.appointmentHour = appointment.getAppointmentHour();
        dto.appointmentMinute = appointment.getAppointmentMinute();
        dto.complaints = appointment.getComplaints();
        dto.isCommercial = appointment.isCommercial();
        dto.promoCode = appointment.getPromoCode();
        dto.reminderOption = appointment.getReminderOption();
        dto.reminderMinutes = appointment.getReminderMinutes();

        dataSource.saveDraft(dto);
    }

    @Override
    public Appointment getDraft() {
        AppointmentDraftDto dto = dataSource.getDraft();
        Patient patient = new Patient(
                dto.fio,
                dto.dobString,
                dto.dobMs,
                dto.omsPolicy,
                dto.phone
        );

        PriceResult price = new PriceResult(0, 0, 0, false, false, !dto.isCommercial);

        return new Appointment(
                patient,
                dto.specialty,
                dto.appointmentDateString,
                dto.appointmentDateMs,
                dto.appointmentTimeString,
                dto.appointmentHour,
                dto.appointmentMinute,
                dto.complaints,
                dto.isCommercial,
                dto.promoCode,
                price,
                dto.reminderOption,
                dto.reminderMinutes
        );
    }

    @Override
    public void clearDraft() {
        dataSource.clearDraft();
    }

    @Override
    public boolean hasDraft() {
        return dataSource.hasDraft();
    }
}
