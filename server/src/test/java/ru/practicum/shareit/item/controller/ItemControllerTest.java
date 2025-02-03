package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentSaveDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    @MockBean
    private final ItemService itemService;
    private ItemDto expectedItem;
    private CommentDto expectedComment;
    private Long userId;

    @BeforeEach
    public void testInit() {
        userId = 1L;

        expectedItem = new ItemDto();
        expectedItem.setId(1L);
        expectedItem.setName("item");
        expectedItem.setDescription("description");
        expectedItem.setAvailable(false);

        expectedComment = new CommentDto();
        expectedComment.setId(1L);
        expectedComment.setText("comment");
        expectedComment.setAuthorName("user1");
    }

    @Test
    void testCreateItem() throws Exception {
        ItemSaveDto itemSaveDto = new ItemSaveDto();
        itemSaveDto.setName("item");
        itemSaveDto.setDescription("description");
        itemSaveDto.setAvailable(false);
        String itemSaveDtoJson = objectMapper.writeValueAsString(itemSaveDto);
        String itemDtoExpectedJson = objectMapper.writeValueAsString(expectedItem);

        when(itemService.createItem(any(Long.class), any(ItemSaveDto.class)))
                .thenReturn(expectedItem);

        mockMvc.perform(post("/items")
                        .header(HEADER_USER_ID, String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(itemSaveDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(itemDtoExpectedJson));

        verify(itemService, times(1)).createItem(any(Long.class), any(ItemSaveDto.class));

    }

    @Test
    void testAddComment() throws Exception {
        Long itemId = expectedItem.getId();
        String path = "/items" + "/" + itemId + "/comment";
        CommentSaveDto comment = new CommentSaveDto();
        comment.setText(expectedComment.getText());
        String commentJson = objectMapper.writeValueAsString(comment);
        String commentExpectedJson = objectMapper.writeValueAsString(expectedComment);

        when(itemService.addComment(any(Long.class), eq(itemId), eq(comment)))
                .thenReturn(expectedComment);

        mockMvc.perform(post(path)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isOk())
                .andExpect(content().json(commentExpectedJson));

        verify(itemService, times(1)).addComment(any(Long.class), eq(itemId), eq(comment));
    }

    @Test
    void testUpdateItem() throws Exception {
        Long itemId = expectedItem.getId();
        String path = "/items" + "/" + itemId;
        ItemSaveDto itemSaveDtoForUpdate = new ItemSaveDto();
        itemSaveDtoForUpdate.setDescription("updated description");
        itemSaveDtoForUpdate.setAvailable(true);
        String itemSaveDtoForUpdateJson = objectMapper.writeValueAsString(itemSaveDtoForUpdate);

        when(itemService.updateItem(any(ItemSaveDto.class), eq(userId), eq(itemId)))
                .thenAnswer(invocationOnMock -> {
                    expectedItem.setDescription(itemSaveDtoForUpdate.getDescription());
                    expectedItem.setAvailable(itemSaveDtoForUpdate.getAvailable());
                    return expectedItem;
                });

        mockMvc.perform(patch(path)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(itemSaveDtoForUpdateJson))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedItem)));

        verify(itemService, times(1)).updateItem(any(ItemSaveDto.class), eq(userId), eq(itemId));
    }

    @Test
    void testGetItem() throws Exception {
        String path = "/items" + "/" + userId;

        when(itemService.getItem(eq(userId)))
                .thenReturn(expectedItem);
        String userDtoExpectedJson = objectMapper.writeValueAsString(expectedItem);

        mockMvc.perform(get(path)
                        .header(HEADER_USER_ID, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(userDtoExpectedJson));

        verify(itemService, times(1)).getItem(eq(userId));
    }

    @Test
    void testGetAllOwnerItems() throws Exception {
        List<ItemDto> itemsExpected = List.of(expectedItem);
        String itemsExpectedJson = objectMapper.writeValueAsString(itemsExpected);

        when(itemService.getItemsWithBookings(eq(userId)))
                .thenReturn(itemsExpected);

        mockMvc.perform(get("/items")
                        .header(HEADER_USER_ID, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(content().json(itemsExpectedJson));

        verify(itemService, times(1)).getItemsWithBookings(eq(userId));
    }

    @Test
    void testSearchItems() throws Exception {
        String searchText = expectedItem.getDescription();
        List<ItemDto> itemsExpected = List.of(expectedItem);
        String itemsExpectedJson = objectMapper.writeValueAsString(itemsExpected);

        when(itemService.searchItems(eq(searchText)))
                .thenReturn(itemsExpected);

        mockMvc.perform(get("/items/search")
                        .param("text", searchText)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(itemsExpectedJson));

        verify(itemService, times(1)).searchItems(eq(searchText));
    }
}
