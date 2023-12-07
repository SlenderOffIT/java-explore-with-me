package ru.practicum.comments.service;

import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto createdComment(NewCommentDto commentDto, long userId, long eventId);

    CommentDto getCommentId(long commentId);

    List<CommentDto> getAllCommentEvent(long eventId, int from, int size);

    CommentDto updateComment(NewCommentDto commentDto, long userId, long commentId);

    void deleteComment(long userId, long eventId);
}
