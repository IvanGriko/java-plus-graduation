package ru.practicum.validation;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToBooleanConverter implements Converter<String, Boolean> {

    @Override
    public Boolean convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Источник данных пуст или равен null");
        }
        if ("true".equalsIgnoreCase(source)) return true;
        if ("false".equalsIgnoreCase(source)) return false;
        throw new IllegalArgumentException("Невозможно преобразовать \"" + source + "\" в булевое значение");
    }
}
