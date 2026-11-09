package com.example.lab4.data.calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CalendarContract;
import androidx.core.content.ContextCompat;
import com.example.lab4.domain.repository.CalendarRepository;
import java.util.TimeZone;

public class AndroidCalendarRepository implements CalendarRepository {
    private final Context context;
    public AndroidCalendarRepository(Context context) { this.context = context.getApplicationContext(); }
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED)
            throw new SecurityException("Нет доступа к календарю");
    }
    @Override public long schedule(String title, long startsAt, boolean weekly) throws Exception {
        checkPermission();
        ContentResolver resolver = context.getContentResolver();
        Long calendarId = null;
        try (Cursor c = resolver.query(CalendarContract.Calendars.CONTENT_URI,
                new String[]{CalendarContract.Calendars._ID},
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + ">=? AND " + CalendarContract.Calendars.VISIBLE + "=1",
                new String[]{String.valueOf(CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR)}, null)) {
            if (c != null && c.moveToFirst()) calendarId = c.getLong(0);
        }
        if (calendarId == null) throw new IllegalStateException("Нет календаря, доступного для записи");
        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        event.put(CalendarContract.Events.TITLE, title);
        event.put(CalendarContract.Events.DTSTART, startsAt);
        event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
        if (weekly) {
            event.put(CalendarContract.Events.DURATION, "PT1H");
            event.put(CalendarContract.Events.RRULE, "FREQ=WEEKLY;INTERVAL=1");
        } else event.put(CalendarContract.Events.DTEND, startsAt + 60 * 60 * 1000L);
        android.net.Uri uri = resolver.insert(CalendarContract.Events.CONTENT_URI, event);
        if (uri == null) throw new IllegalStateException("Не удалось создать событие");
        long id = ContentUris.parseId(uri);
        try {
            ContentValues reminder = new ContentValues();
            reminder.put(CalendarContract.Reminders.EVENT_ID, id);
            reminder.put(CalendarContract.Reminders.MINUTES, 60);
            reminder.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
            if (resolver.insert(CalendarContract.Reminders.CONTENT_URI, reminder) == null)
                throw new IllegalStateException("Не удалось создать напоминание");
        } catch (Exception e) {
            resolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id), null, null);
            throw e;
        }
        return id;
    }
    @Override public void delete(long eventId) {
        checkPermission();
        context.getContentResolver().delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId), null, null);
    }
}
