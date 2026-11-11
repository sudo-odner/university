package com.example.lab4.domain.repository;

public interface CalendarRepository {
    String findConflict(long startsAtMillis, Long excludedEventId) throws Exception;
    long schedule(String title, long startsAtMillis, boolean weekly) throws Exception;
    void delete(long eventId) throws Exception;
}
