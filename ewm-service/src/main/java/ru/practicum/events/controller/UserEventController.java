package ru.practicum.events.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.events.dto.*;
import ru.practicum.events.service.EventService;
import ru.practicum.requests.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class UserEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsByUser(@Positive @PathVariable long userId,
                                               @PositiveOrZero @RequestParam(value = "from", defaultValue = "0", required = false) int from,
                                               @Positive @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        log.info("Поступил запрос на просмотр событий пользователя {}", userId);
        return eventService.getEventsByUser(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addByUser(@Positive @PathVariable long userId,
                                  @Valid @RequestBody NewEventDto event) {
        log.info("Поступил запрос на создание события {} от пользователя {}", event, userId);
        return eventService.addByUser(userId, event);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByIdByUser(@Positive @PathVariable long userId,
                                           @Positive @PathVariable long eventId,
                                           HttpServletRequest request) {
        log.info("Поступил запрос на просмотр события {} пользователя {}", eventId, userId);
        return eventService.getEventByIdByUser(userId, eventId, request);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(@Positive @PathVariable long userId,
                                          @Positive @PathVariable long eventId,
                                          @Valid @RequestBody UpdateEventUserRequest event) {
        log.info("Поступил запрос на изменение события {} пользователя {}", eventId, userId);
        return eventService.updateEventByUser(userId, eventId, event);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsByEventIdByUser(@Positive @PathVariable long userId,
                                                                    @Positive @PathVariable long eventId) {
        log.info("Поступил запрос на просмотр запросов на участие в событии {} пользователя {}", eventId, userId);
        return eventService.getRequestsByEventIdByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult conformRequestsByUser(@Positive @PathVariable long userId,
                                                                @Positive @PathVariable long eventId,
                                                                @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Поступил запрос на изменение запросов на участие в событии {} пользователя {}", eventId, userId);
        return eventService.conformRequestsByUser(userId, eventId, updateRequest);
    }
}
