package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.Appointment;
import com.example.lab2.domain.repository.DraftRepository;

public class SaveDraftUseCase {
    private final DraftRepository draftRepository;

    public SaveDraftUseCase(DraftRepository draftRepository) {
        this.draftRepository = draftRepository;
    }

    public void execute(Appointment appointment) {
        draftRepository.saveDraft(appointment);
    }
}
