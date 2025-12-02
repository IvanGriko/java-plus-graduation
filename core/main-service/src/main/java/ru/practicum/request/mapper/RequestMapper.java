package ru.practicum.request.mapper;

import ru.practicum.event.model.Event;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;
import ru.practicum.user.model.User;

public class RequestMapper {

    public static ParticipationRequestDto toDto(Request request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .requester(request.getRequester().getId())
                .event(request.getEvent().getId())
                .status(request.getStatus())
                .created(request.getCreated())
                .build();
    }

    public static Request toEntity(ParticipationRequestDto dto, User requester, Event event) {
        return Request.builder()
                .id(dto.getId())
                .requester(requester)
                .event(event)
                .status(dto.getStatus())
                .created(dto.getCreated())
                .build();
    }
}
