package ru.practicum.request.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequestDto {

    @NotEmpty(message = "Список 'requestIds' не должен быть пустым")
    List<Long> requestIds;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Поле 'status' не должно быть пустым")
    ParticipationRequestStatus status;

}
