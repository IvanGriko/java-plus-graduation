package ru.practicum.compilation.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.client.UserClientHelper;
import ru.practicum.compilation.dal.Compilation;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dal.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CompilationAdminServiceImpl implements CompilationAdminService {

    TransactionTemplate transactionTemplate;
    CompilationRepository compilationRepository;
    EventRepository eventRepository;
    UserClientHelper userClientHelper;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание новой компиляции с названием {}", newCompilationDto.getTitle());
        Set<Event> events = new HashSet<>();
        Map<Long, UserShortDto> userMap = new HashMap<>();
        if (newCompilationDto.getPinned() == null) newCompilationDto.setPinned(false);
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = transactionTemplate.execute(status -> {
                return new HashSet<>(eventRepository.findAllById(newCompilationDto.getEvents()));
            });
            Set<Long> userIds = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
            userMap = userClientHelper.fetchUserShortDtoMapByUserIdList(userIds);
        }
        Set<Event> eventsFinal = events;
        Map<Long, UserShortDto> userMapFinal = userMap;
        return transactionTemplate.execute(status -> {
            Compilation compilation = Compilation.builder()
                    .pinned(newCompilationDto.getPinned())
                    .title(newCompilationDto.getTitle())
                    .events(eventsFinal)
                    .build();
            compilationRepository.save(compilation);
            log.info("Компиляция с названием {} успешно создана", compilation.getTitle());
            return CompilationMapper.toCompilationDto(compilation, userMapFinal);
        });
    }

    @Override
    @Transactional
    public String deleteCompilation(Long compId) {
        log.info("Удаление компиляции с ID {}", compId);
        if (!compilationRepository.existsById(compId)) {
            log.error("Компиляция с ID {} не найдена", compId);
            throw new NotFoundException("Компиляция с ID " + compId + " не найдена");
        }
        compilationRepository.deleteById(compId);
        log.info("Компиляция с ID {} успешно удалена", compId);
        return "Компиляция с ID " + compId + " успешно удалена";
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationDto updateCompilationDto) {
        log.info("Обновление компиляции с ID {}", compId);
        Set<Long> userIds = transactionTemplate.execute(status -> {
            Compilation compilation = compilationRepository.findById(compId)
                    .orElseThrow(() -> new NotFoundException("Компиляция с ID " + compId + " не найдена"));
            return compilation.getEvents().stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        });
        Map<Long, UserShortDto> userMap = userClientHelper.fetchUserShortDtoMapByUserIdList(userIds);
        return transactionTemplate.execute(status -> {
            Compilation compilation = compilationRepository.findById(compId)
                    .orElseThrow(() -> new NotFoundException("Компиляция с ID " + compId + " не найдена"));
            if (updateCompilationDto.getTitle() != null) {
                compilation.setTitle(updateCompilationDto.getTitle());
            }
            if (updateCompilationDto.getPinned() != null) {
                compilation.setPinned(updateCompilationDto.getPinned());
            }
            if (updateCompilationDto.getEvents() != null && !updateCompilationDto.getEvents().isEmpty()) {
                Set<Event> events = new HashSet<>(eventRepository.findAllById(updateCompilationDto.getEvents()));
                compilation.setEvents(events);
            }
            compilationRepository.save(compilation);
            log.info("Компиляция с ID {} успешно обновлена", compId);
            return CompilationMapper.toCompilationDto(compilation, userMap);
        });
    }

}