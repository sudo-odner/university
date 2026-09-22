package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.ValidationResult;

public class ValidateTimeUseCase {
    public ValidationResult execute(String timeString, Integer hour, Integer minute) {
        if (timeString == null || timeString.trim().isEmpty() || hour == null || minute == null) {
            return ValidationResult.invalid("Выберите время приёма");
        }
        if (hour < 8 || hour >= 20) {
            return ValidationResult.invalid("Время приёма должно быть в интервале 08:00 — 20:00");
        }
        return ValidationResult.valid();
    }
}
