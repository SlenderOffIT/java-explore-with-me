package ru.practicum.events.dto.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.categories.dto.mapper.CategoryMapper;
import ru.practicum.events.dto.EventComment;
import ru.practicum.events.dto.EventFullDto;
import ru.practicum.events.dto.EventShortDto;
import ru.practicum.events.model.Event;
import ru.practicum.location.dto.mapper.LocationMapper;
import ru.practicum.users.dto.mapper.UserMapper;

@UtilityClass
public class EventMapper {
    public static EventShortDto toShortDto(Event event) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapperToCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests().size())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(UserMapper.mapperToShortDto(event.getInitiator()))
                .paid(event.isPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static EventFullDto toFullDto(Event event) {
        EventFullDto dto = EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.mapperToCategoryDto(event.getCategory()))
                .createdOn(event.getCreated())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(UserMapper.mapperToShortDto(event.getInitiator()))
                .location(LocationMapper.mapperToLocationDto(event.getLocation()))
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
        if (event.getConfirmedRequests() != null) {
            dto.setConfirmedRequests(event.getConfirmedRequests().size());
        } else {
            dto.setConfirmedRequests(0);
        }
        return dto;
    }

    public static EventComment toEventComment(Event event) {
        return EventComment.builder()
                .id(event.getId())
                .title(event.getTitle())
                .build();
    }
}
