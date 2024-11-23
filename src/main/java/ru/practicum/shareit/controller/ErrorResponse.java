package ru.practicum.shareit.controller;

public class ErrorResponse {
    String error;

    public String getError() {
        return error;
    }

    public ErrorResponse(String error) {
        this.error = error;
    }
}
