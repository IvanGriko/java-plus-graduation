package ru.practicum.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    private DateTimeFormatter formatter;

    public LocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Autowired
    public void configureFormatter(@Value("${explore-with-me.datetime.format}") String dateTimeFormat) {
        this.formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Override
    public void serialize(LocalDateTime localDateTime,
                          JsonGenerator generator,
                          SerializerProvider provider) throws IOException {
        generator.writeString(localDateTime.format(formatter));
    }
}
