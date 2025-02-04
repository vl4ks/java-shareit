package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentSaveDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    private final User user = new User(1L, "User", "user@email.com");

    private final ItemRequest request = ItemRequest.builder()
            .id(1L)
            .description("description")
            .requester(user)
            .items(new ArrayList<>())
            .build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("description")
            .available(true)
            .owner(user)
            .request(request)
            .build();
    private final ItemDto itemDto = ItemDto.builder()
            .id(1L)
            .name("ItemName")
            .description("description")
            .available(true)
            .id(1L)
            .build();
    private final List<Booking> bookingList = List.of(Booking.builder()
                    .id(1L).item(item).booker(user)
                    .start(LocalDateTime.now().minusHours(2L))
                    .end(LocalDateTime.now().minusHours(1L))
                    .status(BookingStatus.WAITING).build(),
            Booking.builder()
                    .id(2L).item(item).booker(user)
                    .start(LocalDateTime.now().plusHours(1L))
                    .end(LocalDateTime.now().plusHours(2L))
                    .status(BookingStatus.WAITING).build());
    private final Comment comment = Comment.builder()
            .id(1L)
            .text("Text")
            .item(item)
            .author(user)
            .build();
    private final CommentDto commentDto = CommentDto
            .builder()
            .id(1L).text("Text")
            .authorName("User")
            .build();
    private final CommentSaveDto commentSaveDto = CommentSaveDto
            .builder()
            .text("Text")
            .build();
    private final ItemSaveDto itemSaveDto = ItemSaveDto.builder()
            .name("ItemName")
            .description("description")
            .available(true)
            .build();

    @Test
    void testGetItem_Success() {
        Long itemId = item.getId();
        Comment comment2 = Comment.builder()
                .id(2L)
                .text("Text2")
                .item(item)
                .author(user)
                .build();
        List<Comment> comments = List.of(comment, comment2);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findAllByItemId(itemId)).thenReturn(comments);

        ItemDto result = itemService.getItem(itemId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("ItemName", result.getName());
        assertEquals(2, result.getComments().size());
        assertEquals("Text", result.getComments().getFirst().getText());

        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(commentRepository).findAllByItemId(itemId);
    }

    @Test
    void testGetItem_ItemNotFound() {
        Long itemId = 2L;

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItem(itemId));

        Mockito.verify(itemRepository).findById(itemId);
        Mockito.verify(commentRepository, Mockito.never()).findAllByItemId(anyLong());
    }

    @Test
    void testGetItemsWithBookings_ReturnItemsWithBookingsAndComments() {
        Long ownerId = 1L;
        List<Item> items = List.of(item);
        List<Booking> lastBookings = List.of(bookingList.get(0));
        List<Booking> nextBookings = List.of(bookingList.get(1));
        List<Comment> comments = List.of(comment);

        Mockito.when(itemRepository.findAllByOwnerId(ownerId)).thenReturn(items);
        Mockito.when(bookingRepository.findLastBookingsByOwner(eq(ownerId), any(LocalDateTime.class)))
                .thenReturn(lastBookings);
        Mockito.when(bookingRepository.findNextBookingsByOwner(eq(ownerId), any(LocalDateTime.class)))
                .thenReturn(nextBookings);
        Mockito.when(commentRepository.findByItem_Owner_Id(ownerId)).thenReturn(comments);

        ItemDto expectedItemDto = ItemDto.builder()
                .id(1L)
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBookings.getFirst().getStart())
                .nextBooking(nextBookings.getFirst().getStart())
                .comments(List.of(new CommentDto(1L, "Text", "User", null)))
                .build();

        List<ItemDto> actualItems = itemService.getItemsWithBookings(ownerId);

        assertFalse(actualItems.isEmpty());
        assertEquals(1, actualItems.size());
        assertEquals(expectedItemDto, actualItems.getFirst());

        Mockito.verify(itemRepository).findAllByOwnerId(ownerId);
        Mockito.verify(bookingRepository).findLastBookingsByOwner(eq(ownerId), any(LocalDateTime.class));
        Mockito.verify(bookingRepository).findNextBookingsByOwner(eq(ownerId), any(LocalDateTime.class));
        Mockito.verify(commentRepository).findByItem_Owner_Id(ownerId);
    }


    @Test
    void testGetItemsWithBookings_ReturnEmptyList_whenNoItemsFound() {
        Mockito.when(itemRepository.findAllByOwnerId(1L)).thenReturn(List.of());

        List<ItemDto> actualItems = itemService.getItemsWithBookings(1L);

        assertTrue(actualItems.isEmpty());

        Mockito.verify(itemRepository).findAllByOwnerId(1L);
        Mockito.verifyNoInteractions(bookingRepository);
        Mockito.verifyNoInteractions(commentRepository);
    }


    @Test
    void testSearchItems_ReturnListOfItems_whenTextIsValid() {
        String searchText = "ItemName";
        List<Item> foundItems = List.of(item);
        List<ItemDto> expectedItems = List.of(itemDto);

        Mockito.when(itemRepository.searchItems(searchText)).thenReturn(foundItems);

        List<ItemDto> actualItems = itemService.searchItems(searchText);

        assertEquals(expectedItems, actualItems);
        Mockito.verify(itemRepository).searchItems(searchText);
    }

    @Test
    void testSearchItems_ReturnEmptyList_whenTextIsBlank() {
        List<ItemDto> actualItems = itemService.searchItems("   ");

        assertTrue(actualItems.isEmpty());
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testSearchItems_ReturnEmptyList_whenTextIsNull() {
        List<ItemDto> actualItems = itemService.searchItems(null);

        assertTrue(actualItems.isEmpty());
        Mockito.verifyNoInteractions(itemRepository);
    }

    @Test
    void testCreateItem() {
        Mockito.when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(any()))
                .thenReturn(item);

        assertEquals(itemService.createItem(1L, itemSaveDto), itemDto);
    }

    @Test
    void testUpdateItem() {
        ItemSaveDto itemDtoUpdate = ItemSaveDto.builder()
                .name("ItemUpdate")
                .description("DescriptionUpdate")
                .available(true)
                .build();

        ItemDto expectedItemDto = ItemDto.builder()
                .id(1L)
                .name("ItemUpdate")
                .description("DescriptionUpdate")
                .available(true)
                .lastBooking(null)
                .nextBooking(null)
                .comments(null)
                .build();

        Mockito.when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.save(any()))
                .thenReturn(item);

        ItemDto actualItemDto = itemService.updateItem(itemDtoUpdate, 1L, 1L);

        assertEquals(expectedItemDto, actualItemDto);

        Mockito.verify(itemRepository).findById(anyLong());
        Mockito.verify(userRepository).findById(anyLong());
        Mockito.verify(itemRepository).save(any());
    }


    @Test
    void testDeleteItem() {
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        itemService.deleteItem(user, 1L);

        Mockito.verify(itemRepository).findById(1L);
        Mockito.verify(itemRepository).delete(item);
    }

    @Test
    void testCreateComment() {
        Mockito.when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito.when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));

        Mockito.when(bookingRepository.findByItemIdAndBookerIdAndStatusAndEndIsBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(bookingList);
        Mockito.when(commentRepository.save(any()))
                .thenReturn(comment);

        CommentDto testComment = itemService.addComment(1L, 1L, commentSaveDto);

        assertEquals(testComment.getId(), commentDto.getId());
        assertEquals(testComment.getText(), commentDto.getText());
        assertEquals(testComment.getAuthorName(), commentDto.getAuthorName());
    }
}
