package ru.practicum.shareit.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(Class<?> entity, String reason) {
        super(entity.getSimpleName() + " " + reason);
    }

}

