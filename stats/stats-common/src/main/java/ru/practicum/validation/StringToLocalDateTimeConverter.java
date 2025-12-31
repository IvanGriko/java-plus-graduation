package ru.practicum.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {

    private DateTimeFormatter formatter;

    @Value("${explore-with-me.datetime.format}")
    public void injectFormatter(String dateTimeFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Override
    public LocalDateTime convert(String source) {
        if (source.isEmpty()) {
            throw new IllegalArgumentException("Не указан временной диапазон для преобразования");
        }
        try {
            return LocalDateTime.parse(source, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ошибка преобразования строки '" + source + "' в формат LocalDateTime", e);
        }
    }
}