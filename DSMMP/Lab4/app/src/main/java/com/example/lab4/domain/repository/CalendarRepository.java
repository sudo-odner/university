package com.example.lab4.domain.repository;

public interface CalendarRepository {
    long schedule(String title, long startsAtMillis, boolean weekly) throws Exception;
    void delete(long eventId) throws Exception;
}
