package com.example.lab2.domain.repository;

import com.example.lab2.domain.model.Appointment;

public interface DraftRepository {
    void saveDraft(Appointment appointment);
    Appointment getDraft();
    void clearDraft();
    boolean hasDraft();
}
