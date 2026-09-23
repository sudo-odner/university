package com.example.lab2.presentation.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.CalendarContract;

import com.example.lab2.R;
import com.example.lab2.domain.model.Appointment;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarIntentHelper {

    public static Intent createCalendarIntent(Context context, Appointment appointment) {
        if (appointment == null || appointment.getAppointmentDateMs() == null ||
                appointment.getAppointmentHour() == null || appointment.getAppointmentMinute() == null) {
            return null;
        }

        Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCal.setTimeInMillis(appointment.getAppointmentDateMs());

        Calendar localCal = Calendar.getInstance();
        localCal.set(
                utcCal.get(Calendar.YEAR),
                utcCal.get(Calendar.MONTH),
                utcCal.get(Calendar.DAY_OF_MONTH),
                appointment.getAppointmentHour(),
                appointment.getAppointmentMinute(),
                0
        );

        long startMs = localCal.getTimeInMillis();
        long endMs = startMs + (45 * 60 * 1000); // 45 minutes duration

        String title = String.format(Locale.getDefault(),
                "Приём врача: %s — Клиника «Здоровье»", appointment.getDoctorSpecialty());

        String clinicAddress = context.getString(R.string.clinic_address);
        String description = String.format(Locale.getDefault(),
                "Пациент: %s\nПолис ОМС: %s\nКонтактный телефон: %s\nЖалобы: %s\nАдрес: %s, каб. 204",
                appointment.getPatient().getFio(),
                appointment.getPatient().getOmsPolicy(),
                appointment.getPatient().getPhone(),
                appointment.getComplaints(),
                clinicAddress);

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, clinicAddress)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMs)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMs)
                .putExtra(CalendarContract.Events.HAS_ALARM, 1);

        if (appointment.getReminderMinutes() > 0) {
            intent.putExtra(CalendarContract.Reminders.MINUTES, appointment.getReminderMinutes());
        }

        return intent;
    }

    public static boolean canResolveIntent(Context context, Intent intent) {
        if (intent == null) return false;
        PackageManager pm = context.getPackageManager();
        return intent.resolveActivity(pm) != null;
    }
}
