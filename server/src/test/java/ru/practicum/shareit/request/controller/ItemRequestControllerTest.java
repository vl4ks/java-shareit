package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    @MockBean
    private final ItemRequestService service;
    private ItemRequestDto itemRequestExpected;

    @BeforeEach
    public void testInit() {
        itemRequestExpected = new ItemRequestDto();
        itemRequestExpected.setId(1L);
        itemRequestExpected.setDescription("description");
    }

    @Test
    void testCreateItemRequest() throws Exception {
        Long userId = 1L;
        ItemRequestSaveDto itemRequest = new ItemRequestSaveDto();
        itemRequest.setDescription(itemRequestExpected.getDescription());
        String itemRequestJson = objectMapper.writeValueAsString(itemRequest);
        String itemRequestExpectedJson = objectMapper.writeValueAsString(itemRequestExpected);

        when(service.createItemRequest(any(Long.class), any(ItemRequestSaveDto.class)))
                .thenReturn(itemRequestExpected);

        mockMvc.perform(post("/requests")
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(itemRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().json(itemRequestExpectedJson));

        verify(service, times(1))
                .createItemRequest(any(Long.class), any(ItemRequestSaveDto.class));
    }

    @Test
    void testGetAllUserItemRequest() throws Exception {
        Long userId = 10L;
        List<ItemRequestDto> itemRequestsExpected = List.of(itemRequestExpected);
        String itemRequestsExpectedJson = objectMapper.writeValueAsString(itemRequestsExpected);

        when(service.getAllUserItemRequestsWithItems(eq(userId)))
                .thenReturn(itemRequestsExpected);

        mockMvc.perform(get("/requests")
                        .header(HEADER_USER_ID, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(itemRequestsExpectedJson));

        verify(service, times(1)).getAllUserItemRequestsWithItems(eq(userId));
    }

    @Test
    void testGetAllItemRequests() throws Exception {
        Long itemRequestId = itemRequestExpected.getId();
        String path = "/requests" + "/" + itemRequestId;
        String itemRequestExpectedJson = objectMapper.writeValueAsString(itemRequestExpected);

        when(service.getItemRequest(eq(itemRequestId)))
                .thenReturn(itemRequestExpected);

        mockMvc.perform(get(path)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(itemRequestExpectedJson));

        verify(service, times(1)).getItemRequest(eq(itemRequestId));
    }

    @Test
    void testGetAll() throws Exception {
        Long userId = 10L;
        String path = "/requests/all";

        when(service.getAllItemRequests(userId))
                .thenReturn(List.of());

        mockMvc.perform(get(path)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(service, times(1)).getAllItemRequests(userId);
    }
}
