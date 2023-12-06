package ru.practicum.requests.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.enums.State;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.*;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.dto.mapper.ParticipationRequestMapper;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.model.enums.RequestStatus;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getRequestByUserIdByUser(long userId) {
        log.info("Обрабатываем запрос на просмотр заявок пользователя {} в чужих событиях", userId);

        exceptionIfNotUser(userId);
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::mapperToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationRequestDto addRequestByUser(long userId, long eventId) {
        log.info("Обрабатываем запрос от пользователя {} на создание заявки на участие в событии {}", userId, eventId);

        if (!requestRepository.findAllByEventIdAndRequesterId(eventId, userId).isEmpty()) {
            log.warn("Пользователь {} уже участвует в событии {}", userId, eventId);
            throw new ForbiddenArgumentException(String.format("Пользователь %d уже подал заявку в событие %d", userId, eventId));
        }

        Event event = exceptionIfNotEvent(eventId);
        User user = exceptionIfNotUser(userId);

        if (userId == event.getInitiator().getId()) {
            log.warn("Пользователь {} является создателем события {}", userId, eventId);
            throw new ForbiddenArgumentException(String.format("Пользователь %d является создателем события %d", userId, eventId));
        }
        if (event.getState() != State.PUBLISHED) {
            log.warn("Событие {} еще не опубликовано", eventId);
            throw new ForbiddenArgumentException("Событие должно быть опубликовано");
        }
        if (event.getParticipantLimit() <= requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED) && event.getParticipantLimit() != 0) {
            log.warn("Событие {} имеет максимальное подтверждение заявок", eventId);
            throw new ForbiddenArgumentException(String.format("Событие %d имеет максимальное подтверждение заявок", eventId));
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .build();
        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }
        if (!event.isRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        return ParticipationRequestMapper.mapperToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        log.info("Обрабатываем запрос на отмену заявки {} от пользователя {}", requestId, userId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(String.format("Заявка с id %d не существует", requestId)));
        if (request.getRequester().getId() != userId) {
            throw new RuntimeException();
        }
        request.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.mapperToParticipationRequestDto(requestRepository.save(request));
    }

    private User exceptionIfNotUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Пользователя с id {} не существует", userId);
                    return new UserNotFoundException(String.format("Пользователя с id %d не существует.", userId));
                });
    }

    private Event exceptionIfNotEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("События с id {} не существует", eventId);
                    return new CompilationNotFoundException(String.format("События с id %d не существует.", eventId));
                });
    }
}
