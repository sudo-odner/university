package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.ValidationResult;

public class ValidateDobUseCase {
    public ValidationResult execute(String dobString, Long dobMs) {
        if (dobString == null || dobString.trim().isEmpty() || dobMs == null) {
            return ValidationResult.invalid("Укажите дату рождения");
        }
        long now = System.currentTimeMillis();
        if (dobMs > now) {
            return ValidationResult.invalid("Дата рождения не может быть в будущем");
        }
        // Check realistic age (max 125 years in ms ~ 125 * 365.25 * 24 * 3600 * 1000)
        long maxAgeMs = 125L * 365 * 24 * 3600 * 1000;
        if (now - dobMs > maxAgeMs) {
            return ValidationResult.invalid("Проверьте правильность указанного года рождения");
        }
        return ValidationResult.valid();
    }
}
