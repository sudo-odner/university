package com.example.lab2.domain.usecase;

import com.example.lab2.domain.repository.DraftRepository;

public class ClearDraftUseCase {
    private final DraftRepository draftRepository;

    public ClearDraftUseCase(DraftRepository draftRepository) {
        this.draftRepository = draftRepository;
    }

    public void execute() {
        draftRepository.clearDraft();
    }
}
