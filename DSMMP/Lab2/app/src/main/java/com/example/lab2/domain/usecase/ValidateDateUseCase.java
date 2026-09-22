package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.ValidationResult;

public class ValidateDateUseCase {
    public ValidationResult execute(String dateString, Long dateMs) {
        if (dateString == null || dateString.trim().isEmpty() || dateMs == null) {
            return ValidationResult.invalid("Выберите дату приёма");
        }
        return ValidationResult.valid();
    }
}
