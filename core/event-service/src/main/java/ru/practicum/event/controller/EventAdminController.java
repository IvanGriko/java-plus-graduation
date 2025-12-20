package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.event.EventAdminApi;
import ru.practicum.dto.event.EventAdminParams;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.event.UpdateEventDto;
import ru.practicum.event.service.EventAdminService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventAdminController implements EventAdminApi {

    private final EventAdminService eventAdminService;

    @Override
    public Collection<EventFullDto> getAllEventsByParams(List<Long> users,
                                                         List<State> states,
                                                         List<Long> categories,
                                                         LocalDateTime rangeStart,
                                                         LocalDateTime rangeEnd,
                                                         Integer from,
                                                         Integer size
    ) {
        log.info("Получение всех событий администраторами");
        EventAdminParams params = EventAdminParams.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
        return eventAdminService.getAllEventsByParams(params);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventDto updateEventDto) {
        log.info("Администратор обновляет событие с ID {}", eventId);
        return eventAdminService.updateEventByAdmin(eventId, updateEventDto);
    }
}