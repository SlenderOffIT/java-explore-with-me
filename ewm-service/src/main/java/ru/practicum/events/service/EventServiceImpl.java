package ru.practicum.events.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.HitDto;
import ru.practicum.StatsDto;
import ru.practicum.StatsClient;
import ru.practicum.categories.model.Category;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.events.dto.*;
import ru.practicum.events.dto.mapper.EventMapper;
import ru.practicum.events.model.Event;
import ru.practicum.events.model.enums.AdminStateAction;
import ru.practicum.events.model.enums.SortEventsBy;
import ru.practicum.events.model.enums.State;
import ru.practicum.events.model.enums.UserStateAction;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.*;
import ru.practicum.location.dto.LocationDto;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.dto.mapper.ParticipationRequestMapper;
import ru.practicum.requests.model.ParticipationRequest;
import ru.practicum.requests.model.enums.NewRequestStatus;
import ru.practicum.requests.model.enums.RequestStatus;
import ru.practicum.requests.repository.RequestRepository;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    @Autowired
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getEventsByUser(long userId, int from, int size) {
        log.info("Обрабатываем запрос на просмотр событий пользователя {}", userId);

        exceptionIfNotUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto addByUser(long userId, NewEventDto dto) {
        log.info("Обрабатываем запрос на создание события {} от пользователя {}", dto, userId);

        User user = exceptionIfNotUser(userId);
        Category category = exceptionIfNotCategory(dto.getCategory());

        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("eventDate должно содержать дату, которая еще не наступила {}", dto.getEventDate());
            throw new ForbiddenArgumentException(String.format("eventDate должно содержать дату, которая еще не наступила %s", dto.getEventDate()));
        }

        Event event = Event.builder()
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .title(dto.getTitle())
                .initiator(user)
                .category(category)
                .location(locationRepository.findByLonAndLat(dto.getLocation().getLon(), dto.getLocation().getLat())
                        .orElse(addLocation(dto.getLocation())))
                .created(LocalDateTime.now())
                .eventDate(dto.getEventDate())
                .views(0)
                .state(State.PENDING)
                .build();

        if (dto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        } else {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getPaid() == null) {
            event.setPaid(false);
        } else {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        } else {
            event.setParticipantLimit(dto.getParticipantLimit());
        }

        return EventMapper.toFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByIdByUser(long userId, long eventId, HttpServletRequest request) {
        log.info("Обрабатываем запрос на просмотр события {} пользователя {}", eventId, userId);
        return EventMapper.toFullDto(exceptionIfNotEvent(eventId));
    }

    @Override
    public EventFullDto updateEventByUser(long userId, long eventId, UpdateEventUserRequest dto) {
        log.info("Обрабатываем запрос на изменение события {} пользователя {}", eventId, userId);

        Event event = exceptionIfNotEvent(eventId);
        exceptionIfNotUser(userId);

        if (event.getState().equals(State.PUBLISHED)) {
            log.warn("Изменить можно только отмененный или события в ожидании {}", eventId);
            throw new ForbiddenArgumentException("Изменить можно только отмененный или события в ожидании");
        }
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("eventDate должно содержать дату, которая еще не наступила {}", event.getEventDate());
            throw new ForbiddenArgumentException(String.format("eventDate должно содержать дату, которая еще не наступила %s", event.getEventDate()));
        }

        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                log.warn("EventDate должна быть не раньше чем через 2 часа от текущего времени {}", dto.getEventDate());
                throw new ValidationRequestException(String.format("EventDate должна быть не раньше чем через 2 часа от текущего времени %s", dto.getEventDate()));
            }
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getCategory() != null) {
            event.setCategory(exceptionIfNotCategory(dto.getCategory()));
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getLocation() != null) {
            event.setLocation(locationRepository.findByLonAndLat(dto.getLocation().getLon(), dto.getLocation().getLat())
                    .orElse(addLocation(dto.getLocation())));
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getStateAction() != null) {
            if (dto.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
                event.setState(State.PENDING);
            }
            if (dto.getStateAction() == UserStateAction.CANCEL_REVIEW) {
                event.setState(State.CANCELED);
            }
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }

        return EventMapper.toFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEventsPublic(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, SortEventsBy sort, int from, int size,
                                               HttpServletRequest request) {
        log.info("Обрабатываем запрос на получение событий с возможностью фильтрации");

        List<Event> events;
        Pageable pageable = PageRequest.of(from / size, size);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        dateValidator(rangeStart, rangeEnd);
        List<Specification<Event>> specifications = new ArrayList<>();

        if (categories != null) {
            List<Category> categoryList = categories.stream()
                    .map(id -> categoryRepository.findById(id)
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            specifications.add(categoryIdIn(categoryList));
        }

        specifications.add(paid == null ? null : paidIs(paid));
        specifications.add(eventDateGreaterOrEquals(rangeStart));
        specifications.add(rangeEnd == null ? null : eventDateLess(rangeEnd));
        specifications.add(stateIn(List.of(State.PUBLISHED)));
        Specification<Event> specification = specifications.stream()
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElseThrow();

        if (text == null) {
            events = eventRepository.findAll(specification, pageable).stream().collect(Collectors.toList());
        } else {
            events = new ArrayList<>(eventRepository.findAllByText(text, specification, pageable));
        }

        if (sort != null) {
            switch (sort) {
                case EVENT_DATE:
                    events.sort(Comparator.comparing(Event::getEventDate));
                    break;
                case VIEWS:
                    events.sort(Comparator.comparing(Event::getViews));
                    break;
                default:
                    throw new ValidationRequestException("Сортировка не действительна");
            }
        }

        if (onlyAvailable) {
            log.info("Просмотр событий text {}, categories {}, paid {}, rangeStart {}, rangeEnd {}, onlyAvailable {}, sort by {}",
                    text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);
            addHit(request);
            return events.stream()
                    .filter(event -> (event.getParticipantLimit() > event.getConfirmedRequests().size()) || event.getParticipantLimit() == 0)
                    .map(EventMapper::toShortDto)
                    .collect(Collectors.toList());
        }

        log.info("Просмотр событий text {}, categories {}, paid {}, rangeStart {}, rangeEnd {}, onlyAvailable {}, sort by {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort);
        addHit(request);

        return events.stream()
                .map(EventMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByIdPublic(long eventId, HttpServletRequest request) {
        log.info("Обрабатываем запрос на получение событий с id {}", eventId);

        Event event = exceptionIfNotEvent(eventId);
        if (event.getState() != State.PUBLISHED) {
            log.warn("События с id {} не существует", eventId);
            throw new EventNotFoundException(String.format("События с id %d не существует.", eventId));
        }

        Integer views = viewsCount(eventId);
        addHit(request);
        Integer updatedViews = viewsCount(eventId);
        if (views < updatedViews) {
            event.setViews(event.getViews() + 1);
        }

        return EventMapper.toFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<State> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               int from, int size) {
        log.info("Обрабатываем запрос на поиск событий");

        List<Event> events;
        dateValidator(rangeStart, rangeEnd);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Specification<Event>> specifications = new ArrayList<>();

        if (users != null) {
            List<User> userList = users.stream()
                    .map(id -> userRepository.findById(id)
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            specifications.add(initiatorIdIn(userList));
        }
        if (categories != null) {
            List<Category> categoryList = categories.stream()
                    .map(id -> categoryRepository.findById(id)
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            specifications.add(categoryIdIn(categoryList));
        }

        specifications.add(states == null ? null : stateIn(states));
        specifications.add(rangeStart == null ? null : eventDateGreaterOrEquals(rangeStart));
        specifications.add(rangeEnd == null ? null : eventDateLess(rangeEnd));
        Specification<Event> specification = specifications.stream()
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(null);

        if (specification != null) {
            events = eventRepository.findAll(specification, pageable).toList();
        } else {
            events = eventRepository.findAll(pageable).toList();
        }

        return events.stream()
                .map(EventMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventByAdmin(long eventId, UpdateEventAdminRequest dto) {
        log.info("Обрабатываем запрос на изменение события {}", eventId);

        Event event = exceptionIfNotEvent(eventId);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            log.warn("eventDate должно содержать дату, которая еще не наступила {}", event.getEventDate());
            throw new ForbiddenArgumentException(String.format("eventDate должно содержать дату, которая еще не наступила %s", event.getEventDate()));
        }

        if (dto.getEventDate() != null) {
            if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                log.warn("eventDate должно содержать дату, которая еще не наступила {}", event.getEventDate());
                throw new ForbiddenArgumentException(String.format("eventDate должно содержать дату, которая еще не наступила %s", dto.getEventDate()));
            }
            event.setEventDate(dto.getEventDate());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            event.setCategory(exceptionIfNotCategory(dto.getCategory()));
        }
        if (dto.getLocation() != null) {
            event.setLocation(locationRepository.findByLonAndLat(dto.getLocation().getLon(), dto.getLocation().getLat())
                    .orElse(addLocation(dto.getLocation())));
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        }
        if (dto.getStateAction() != null) {
            if (dto.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                if (event.getState() != State.PENDING) {
                    log.warn("Не возможно опубликовать события т.к оно не в правильном состоянии {}", event.getState());
                    throw new ForbiddenArgumentException(String.format("Не возможно опубликовать события т.к оно не в правильном состоянии %s", event.getState()));
                }
                event.setState(State.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                if (event.getState() == State.PUBLISHED) {
                    log.warn("Не возможно опубликовать события т.к оно не в правильном состоянии {}", event.getState());
                    throw new ForbiddenArgumentException(String.format("Не возможно опубликовать события т.к оно не в правильном состоянии %s", event.getState()));
                }
                event.setState(State.CANCELED);
            }
        }
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }
        return EventMapper.toFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEventIdByUser(long userId, long eventId) {
        log.info("Обрабатываем запрос на просмотр запросов на участие в событии {} пользователя {}", eventId, userId);

        exceptionIfNotEvent(eventId);
        exceptionIfNotUser(userId);
        return requestRepository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::mapperToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult conformRequestsByUser(long userId, long eventId, EventRequestStatusUpdateRequest updateRequest) {
        log.info("Обрабатываем запрос на изменение запросов на участие в событии {} пользователя {}", eventId, userId);

        exceptionIfNotUser(userId);
        Event event = exceptionIfNotEvent(eventId);
        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            log.warn("Не требуется принимать запросы, пре-модерация отключена у события {}", eventId);
            throw new ForbiddenArgumentException("Не требуется принимать запросы, пре-модерация отключена");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.getRequestIds());
        int limit = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (limit == event.getParticipantLimit()) {
            log.warn("Достигнут лимит участников у события {}", eventId);
            throw new ForbiddenArgumentException("Достигнут лимит участников");
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult(new ArrayList<>(), new ArrayList<>());
        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                log.warn("Запрос должен иметь статус ожидания");
                throw new ForbiddenArgumentException("Запрос должен иметь статус ожидания");
            }
            if (updateRequest.getStatus() == NewRequestStatus.CONFIRMED) {
                if (limit < event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    result.getConfirmedRequests().add(ParticipationRequestMapper.mapperToParticipationRequestDto(request));
                    limit++;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    result.getRejectedRequests().add(ParticipationRequestMapper.mapperToParticipationRequestDto(request));
                }
            } else {
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(ParticipationRequestMapper.mapperToParticipationRequestDto(request));
            }
        }
        requestRepository.saveAll(requests);
        return result;
    }

    private Event exceptionIfNotEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("События с id {} не существует", eventId);
                    return new CompilationNotFoundException(String.format("События с id %d не существует.", eventId));
                });
    }

    private User exceptionIfNotUser(long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("Пользователя с id {} не существует", userId);
                return new UserNotFoundException(String.format("Пользователя с id %d не существует.", userId));
            });
    }

    private Category exceptionIfNotCategory(long catId) {
        return categoryRepository.findById(catId)
            .orElseThrow(() ->  {
                log.warn("Категории с id {} не существует", catId);
                return new CategoryNotFoundException(String.format("Категории с id %d не существует.", catId));
            });
    }

    private Integer viewsCount(long eventId) {
        ResponseEntity<StatsDto[]> response = statsClient.get(LocalDateTime.now().minusYears(1),
                LocalDateTime.now(),
                new String[]{"/events/" + eventId},
                true);
        int views = 0;
        Optional<StatsDto> stat;
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            stat = Arrays.stream(response.getBody()).findFirst();
            if (stat.isPresent()) {
                views = Math.toIntExact(stat.get().getHits());
            }
        }
        return views;
    }

    private void addHit(HttpServletRequest request) {
        statsClient.hit(HitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Location addLocation(LocationDto dto) {
        return locationRepository.save(Location.builder()
                .lon(dto.getLon())
                .lat(dto.getLat())
                .build());
    }

    private void dateValidator(LocalDateTime start, LocalDateTime end) {
        if (end != null && start.isAfter(end)) {
            throw new ValidationRequestException("Старт не должно быть позже окончания");
        }
    }

    private Specification<Event> initiatorIdIn(List<User> users) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("initiator")).value(users);
    }

    private Specification<Event> stateIn(List<State> states) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("state")).value(states);
    }

    private Specification<Event> categoryIdIn(List<Category> categories) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.in(root.get("category")).value(categories);
    }

    private Specification<Event> eventDateGreaterOrEquals(LocalDateTime rangeStart) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart);
    }

    private Specification<Event> eventDateLess(LocalDateTime rangeEnd) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd);
    }

    private Specification<Event> paidIs(Boolean paid) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paid"), paid);
    }
}