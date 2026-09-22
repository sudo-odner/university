package com.example.lab2.domain.usecase;

import com.example.lab2.domain.model.ValidationResult;
import java.util.regex.Pattern;

public class ValidateFioUseCase {
    private static final Pattern FIO_PATTERN =
            Pattern.compile("^[а-яА-ЯёЁa-zA-Z]+([\\s-]+[а-яА-ЯёЁa-zA-Z]+)+$");

    public ValidationResult execute(String fio) {
        if (fio == null || fio.trim().isEmpty()) {
            return ValidationResult.invalid("Введите ФИО пациента");
        }
        String trimmed = fio.trim();
        if (!FIO_PATTERN.matcher(trimmed).matches()) {
            return ValidationResult.invalid("Введите корректные ФИО (не менее 2 слов, только буквы)");
        }
        return ValidationResult.valid();
    }
}
