package ru.practicum.comments.dto;

import ru.practicum.comments.model.Comment;

import static ru.practicum.events.dto.mapper.EventMapper.toEventComment;

public class CommentMapper {

    public static Comment toComment(NewCommentDto commentDto) {
        return new Comment(commentDto.getText());
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .text(comment.getText())
                .id(comment.getId())
                .author(comment.getAuthor().getName())
                .event(toEventComment(comment.getEvent()))
                .created(comment.getCreated())
                .build();
    }
}
