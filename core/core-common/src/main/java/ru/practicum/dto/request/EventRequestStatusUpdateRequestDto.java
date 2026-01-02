package ru.practicum.dto.request;

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

    @NotEmpty(message = "Список идентификаторов заявок не должен быть пустым")
    List<Long> requestIds;

    @NotNull(message = "Статус заявки не должен быть пустым")
    ParticipationRequestStatus status;
}