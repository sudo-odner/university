package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.ValidationResult;

public class ValidateSpecialtyUseCase {
    public ValidationResult execute(String specialty) {
        if (specialty == null || specialty.trim().isEmpty()) {
            return ValidationResult.invalid("Пожалуйста, выберите специализацию врача");
        }
        return ValidationResult.valid();
    }
}
