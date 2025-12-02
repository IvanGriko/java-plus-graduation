package ru.practicum.validation;

import jakarta.annotation.Resource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebConfig implements WebMvcConfigurer {

    @Resource
    StringToLocalDateTimeConverter stringToLocalDateTimeConverter;

    @Resource
    StringToBooleanConverter stringToBooleanConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToLocalDateTimeConverter);
        registry.addConverter(stringToBooleanConverter);
    }
}
