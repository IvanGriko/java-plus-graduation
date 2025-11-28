package ru.practicum.validation;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebConfig implements WebMvcConfigurer {

    StringToLocalDateTimeConverter stringToLocalDateTimeConverter;
    StringToBooleanConverter stringToBooleanConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLocalDateTimeConverter);
        registry.addConverter(stringToBooleanConverter);
    }
}
