package ru.practicum.exceptions.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exceptions.*;
import ru.practicum.exceptions.dto.ApiError;
import org.hibernate.exception.ConstraintViolationException;

import javax.xml.bind.ValidationException;
import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler({UserNotFoundException.class, RequestNotFoundException.class, EventNotFoundException.class,
            CompilationNotFoundException.class, LocationNotFoundException.class, CategoryNotFoundException.class,
            CommentNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final Exception e) {
        log.warn("{}!, {}", e.getClass().getSimpleName(), e.getMessage());
        return new ApiError(HttpStatus.NOT_FOUND, "The required object was not found.",
                e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler({ValidationException.class, ValidationRequestException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final Exception e) {
        log.warn("{}!, {}", e.getClass().getSimpleName(), e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, "Incorrectly made request.",
                e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintViolationException(final ConstraintViolationException e) {
        log.warn("ConstraintViolationException!, {}", e.getMessage());
        return new ApiError(HttpStatus.CONFLICT, "Integrity constraint has been violated.",
                e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleForbiddenArgumentException(final ForbiddenArgumentException e) {
        log.warn("ForbiddenArgumentException!, {}", e.getMessage());
        return new ApiError(HttpStatus.FORBIDDEN, "For the requested operation the conditions are not met.",
                e.getMessage(), LocalDateTime.now());
    }
}
