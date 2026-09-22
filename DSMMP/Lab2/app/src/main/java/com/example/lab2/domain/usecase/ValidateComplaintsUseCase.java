package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.ValidationResult;

public class ValidateComplaintsUseCase {
    public ValidationResult execute(String complaints) {
        if (complaints == null || complaints.trim().isEmpty()) {
            return ValidationResult.invalid("Опишите жалобы и симптомы");
        }
        if (complaints.trim().length() < 5) {
            return ValidationResult.invalid("Описание жалоб должно содержать не менее 5 символов");
        }
        return ValidationResult.valid();
    }
}
