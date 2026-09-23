package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.ValidationResult;

public class ValidatePhoneUseCase {
    public ValidationResult execute(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return ValidationResult.invalid("Введите контактный телефон");
        }
        String trimmed = phone.trim();
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 10 || digitsOnly.length() > 15) {
            return ValidationResult.invalid("Формат: +7 (900) 000-00-00 (от 10 до 15 цифр)");
        }
        if (digitsOnly.length() == 11 && !digitsOnly.startsWith("7") && !digitsOnly.startsWith("8")) {
            return ValidationResult.invalid("Проверьте код страны в номере телефона");
        }
        return ValidationResult.valid();
    }
}
