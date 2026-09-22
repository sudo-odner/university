package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.ValidationResult;

public class ValidateOmsUseCase {
    public ValidationResult execute(String oms) {
        if (oms == null || oms.trim().isEmpty()) {
            return ValidationResult.invalid("Введите номер полиса ОМС");
        }
        String trimmed = oms.trim();
        if (trimmed.length() != 16 || !trimmed.matches("^\\d{16}$")) {
            return ValidationResult.invalid("Номер полиса ОМС должен состоять ровно из 16 цифр");
        }
        return ValidationResult.valid();
    }
}
