package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentMapper;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.exceptions.CommentNotFoundException;
import ru.practicum.exceptions.CompilationNotFoundException;
import ru.practicum.exceptions.UserNotFoundException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.comments.dto.CommentMapper.toComment;
import static ru.practicum.comments.dto.CommentMapper.toCommentDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentDto createdComment(NewCommentDto commentDto, long userId, long eventId) {
        log.info("Обрабатываем запрос на создание комментария в событию {} от пользователя {}", eventId, userId);

        Comment comment = toComment(commentDto);
        User user = exceptionIfNotUser(userId);
        Event event = exceptionIfNotEvent(eventId);

        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());

        return toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto getCommentId(long commentId) {
        log.info("Обрабатываем запрос на просмотр комментария {}", commentId);

        return toCommentDto(exceptionIfNotComment(commentId));
    }

    @Override
    public List<CommentDto> getAllCommentEvent(long eventId, int from, int size) {
        log.info("Обрабатываем запрос на просмотр всех комментариев события {}", eventId);

        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.findAllByEventId(eventId, pageable).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto updateComment(NewCommentDto commentDto, long userId, long commentId) {
        log.info("Обрабатываем запрос на изменение комментария {}", commentId);

        Comment comment = exceptionIfNotComment(commentId);
        User user = exceptionIfNotUser(userId);

        if (comment.getAuthor().getId() != user.getId()) {
            log.warn("Пользователь с id {} пытается изменить не свой комментарий с id {}.", userId, commentId);
            throw new CommentNotFoundException("Вы не являетесь создателем данного комментария");
        }

        comment.setText(commentDto.getText());

        return toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(long userId, long commentId) {
        log.info("Обрабатываем запрос на удаление комментария {}", commentId);

        Comment comment = exceptionIfNotComment(commentId);
        User user = exceptionIfNotUser(userId);

        if (comment.getAuthor().getId() != user.getId()) {
            log.warn("Пользователь с id {} пытается удалить не свой комментарий с id {}.", userId, commentId);
            throw new CommentNotFoundException("Вы не являетесь создателем данного комментария");
        }

        commentRepository.deleteById(commentId);
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

    private Comment exceptionIfNotComment(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Комментария с id {} не существует", commentId);
                    return new UserNotFoundException(String.format("Комментария с id %d не существует.", commentId));
                });
    }
}
