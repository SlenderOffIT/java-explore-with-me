package ru.practicum.requests.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.service.RequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class UserRequestController {
    private final RequestService service;

    @GetMapping
    public List<ParticipationRequestDto> getByUserId(@PathVariable(name = "userId") long userId) {
        log.info("Поступил запрос на просмотр заявок пользователя {} в чужих событиях", userId);
        return service.getRequestByUserIdByUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto add(@PathVariable(name = "userId") long userId,
                                       @RequestParam(value = "eventId") long eventId) {
        log.info("Поступил запрос от пользователя {} на создание заявки на участие в событии {}", userId, eventId);
        return service.addRequestByUser(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable(name = "userId") long userId,
                       @PathVariable(name = "requestId") long requestId) {
        log.info("Поступил запрос на отмену заявки {} от пользователя {}", requestId, userId);
        return service.cancelRequest(userId, requestId);
    }
}