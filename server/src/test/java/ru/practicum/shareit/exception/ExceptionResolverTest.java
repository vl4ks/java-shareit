package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = {ExceptionResolver.class})
@Import(ExceptionResolverTest.TestController.class)
@ExtendWith(SpringExtension.class)
class ExceptionResolverTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHandleNotFound_Return404WithErrorMessage() throws Exception {
        mockMvc.perform(get("/exception/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"));
    }

    @Test
    void testHandleValidation_Return400WithErrorMessage() throws Exception {
        mockMvc.perform(get("/exception/validation-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации: Некорректные данные"));
    }

    @Test
    void testHandleForbidden_Return403WithErrorMessage() throws Exception {
        mockMvc.perform(get("/exception/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Ошибка доступа: Доступ запрещен"));
    }

    @Test
    void testHandleConflict_Return409WithErrorMessage() throws Exception {
        mockMvc.perform(get("/exception/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict: Конфликт данных"));
    }

    @Test
    void testHandleThrowable_Return500WithGenericErrorMessage() throws Exception {
        mockMvc.perform(get("/exception/internal-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Произошла непредвиденная ошибка."));
    }

    @RestController
    @RequestMapping("/exception")
    static class TestController {
        @GetMapping("/not-found")
        public void throwNotFoundException() {
            throw new NotFoundException("Ресурс не найден");
        }

        @GetMapping("/validation-error")
        public void throwValidationException() {
            throw new ValidationException("Некорректные данные");
        }

        @GetMapping("/forbidden")
        public void throwForbiddenException() {
            throw new ForbiddenException("Доступ запрещен");
        }

        @GetMapping("/conflict")
        public void throwConflictException() {
            throw new ConflictException("Конфликт данных");
        }

        @GetMapping("/internal-error")
        public void throwInternalError() {
            throw new RuntimeException("Произошла непредвиденная ошибка.");
        }
    }

}
