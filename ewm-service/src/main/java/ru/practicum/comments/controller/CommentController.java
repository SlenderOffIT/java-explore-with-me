package ru.practicum.comments.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/user/{userId}/events/{eventId}/comment/")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto postComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                  @PathVariable @Positive long userId, @PathVariable @Positive long eventId) {
        log.info("Поступил запрос на создание комментария в событию {} от пользователя {}", eventId, userId);
        return commentService.createdComment(newCommentDto, userId, eventId);
    }

    @GetMapping("/comment/{commentId}")
    public CommentDto getComment(@PathVariable @Positive long commentId) {
        log.info("Поступил запрос на просмотр комментария {}", commentId);
        return commentService.getCommentId(commentId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getAllCommentEvent(@PathVariable @Positive long eventId,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                               @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("Поступил запрос на просмотр всех комментариев события {}", eventId);
        return commentService.getAllCommentEvent(eventId, from, size);
    }

    @PatchMapping("/user/{userId}/comment/{commentId}")
    public CommentDto patchComment(@RequestBody @Valid NewCommentDto newCommentDto,
                                   @PathVariable @Positive long userId, @PathVariable @Positive long commentId) {
        log.info("Поступил запрос на изменение комментария {}", commentId);
        return commentService.updateComment(newCommentDto, userId, commentId);
    }

    @DeleteMapping("/user/{userId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive long userId, @PathVariable @Positive long commentId) {
        log.info("Поступил запрос на удаление комментария {}", commentId);
        commentService.deleteComment(userId, commentId);
    }
}
