package ru.practicum.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private DateTimeFormatter formatter;

    public LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Autowired
    public void configureFormatter(@Value("${explore-with-me.datetime.format}") String dateTimeFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        try {
            String inputDate = parser.getText();
            return LocalDateTime.parse(inputDate, formatter);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new IOException("Ошибка десериализации даты: неверный формат или пустое значение", exception);
        }
    }
}
