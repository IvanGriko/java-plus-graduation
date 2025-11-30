package ru.practicum.event.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventAdminParams;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.State;
import ru.practicum.event.dto.UpdateEventDto;
import ru.practicum.event.service.EventAdminService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

//@RestController
//@RequestMapping("/admin/events")
//@RequiredArgsConstructor
//@Slf4j
//@Validated
//public class EventAdminController {
//
//    private final EventAdminService eventAdminService;
//
//    @GetMapping
//    public Collection<EventFullDto> getAllEventsByParams(
//            @RequestParam(required = false) List<Long> users,
//            @RequestParam(required = false) List<State> states,
//            @RequestParam(required = false) List<Long> categories,
//            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
//            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
//            @RequestParam(defaultValue = "0") @PositiveOrZero long from,
//            @RequestParam(defaultValue = "10") @Positive long size
//    ) {
//        EventAdminParams params = EventAdminParams.builder()
//                .users(users)
//                .states(states)
//                .categories(categories)
//                .rangeStart(rangeStart)
//                .rangeEnd(rangeEnd)
//                .from(from)
//                .size(size)
//                .build();
//        log.info("Получение событий администратора с параметрами: {}", params);
//        return eventAdminService.getAllEventsByParams(params);
//    }
//
//    @PatchMapping("/{eventId}")
//    public EventFullDto updateEventByAdmin(
//            @PathVariable Long eventId,
//            @RequestBody @Valid UpdateEventDto updateEventDto
//    ) {
//        log.info("Обновление события с ID={} с данными: {}", eventId, updateEventDto);
//        return eventAdminService.updateEventByAdmin(eventId, updateEventDto);
//    }
//}

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class EventAdminController {

    private final EventAdminService eventAdminService;

    @GetMapping
    Collection<EventFullDto> getAllEventsByParams(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<State> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
            @RequestParam(defaultValue = "10") @Positive Long size
    ) {
        EventAdminParams params = EventAdminParams.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();
        log.info("Calling to endpoint /admin/events GetMapping for params: " + params.toString());
        return eventAdminService.getAllEventsByParams(params);
    }

    @PatchMapping("/{eventId}")
    EventFullDto updateEventByAdmin(
            @PathVariable Long eventId,
            @RequestBody @Valid UpdateEventDto updateEventDto
    ) {
        log.info("Calling to endpoint /admin/events/{eventId} PatchMapping for eventId: " + eventId + "."
                + " UpdateEvent: " + updateEventDto.toString());
        return eventAdminService.updateEventByAdmin(eventId, updateEventDto);
    }
}