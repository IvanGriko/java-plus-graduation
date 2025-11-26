package ru.practicum.request.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.request.model.ParticipationRequestStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateRequestDto {

    @NotEmpty(message = "Field 'requestIds' shouldn't be empty")
    List<Long> requestIds;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Field 'status' shouldn't be null")
    ParticipationRequestStatus status;

}
