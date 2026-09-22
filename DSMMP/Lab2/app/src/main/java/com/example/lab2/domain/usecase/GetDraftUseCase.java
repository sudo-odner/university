package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.Appointment;
import com.example.lab2.domain.repository.DraftRepository;

public class GetDraftUseCase {
    private final DraftRepository draftRepository;

    public GetDraftUseCase(DraftRepository draftRepository) {
        this.draftRepository = draftRepository;
    }

    public Appointment execute() {
        return draftRepository.getDraft();
    }

    public boolean hasDraft() {
        return draftRepository.hasDraft();
    }
}
