package ru.practicum.shareit.item.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enumeration.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.CustomBadRequestException;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.api.CommentRepository;
import ru.practicum.shareit.item.repository.api.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private ItemServiceImpl itemService;

    private UserDto user;
    private ItemDto item;
    private ItemDto itemDto;
    private CommentDto comment;
    private CommentDto commentDto;
    private BookingResponseDto booking;

    @BeforeEach
    void setUp() {
        user = new UserDto(1L, "User", "user@example.com");
        item = new ItemDto(1L, "Item", "Description", true, user, null, null, null, null);
        itemDto = new ItemDto(1L, "Item", "Description", true, null, null, null, null, null);
        comment = new CommentDto(1L, "Comment", item, user, null, null);
        commentDto = new CommentDto(1L, "Comment", item, user, "User", null);
        booking = new BookingResponseDto(1L, null, null, item, user, null);
    }

    @Test
    void testSaveItemWhenAllDependenciesAvailableThenSaveItem() {
        Long ownerId = 1L;
        Long requestId = 2L;
        ItemDto itemDto = ItemDto.builder()
                .name("Item Name")
                .description("Item Description")
                .available(true)
                .requestId(requestId)
                .build();
        User owner = new User(ownerId, "Owner Name", "owner@example.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item savedItem = invocation.getArgument(0);
            savedItem.setId(1L);
            return savedItem;
        });

        ItemDto savedItemDto = itemService.saveItem(ownerId, itemDto);

        assertNotNull(savedItemDto);
        assertEquals(requestId, savedItemDto.getRequestId());
    }

    @Test
    void testUpdateItemWhenAllDependenciesAvailableThenItemUpdated() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(UserMapper.INSTANCE.toUser(user)));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(ItemMapper.INSTANCE.toItem(item)));
        when(itemRepository.save(any(Item.class))).thenReturn(ItemMapper.INSTANCE.toItem(item));

        ItemDto result = itemService.updateItem(user.getId(), item.getId(), itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testWhenUpdateItemWithWrongOwnerIdThenMustThrowException() {
        Long ownerId = 1L;
        Long wrongOwnerId = 2L;
        Long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("Updated Item Name")
                .description("Updated Description")
                .available(true)
                .build();
        User existOwner = new User(ownerId, "Owner Name", "owner@example.com");
        User wrongOwner = new User(wrongOwnerId, "Wrong Owner Name", "wrongowner@example.com");
        Item existItem = new Item(itemId, "Item Name", "Item Description", true, wrongOwner, null);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(existOwner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existItem));

        assertThrows(CustomEntityNotFoundException.class, () -> itemService.updateItem(ownerId, itemId, itemDto));

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void testGetByIdWhenAllDependenciesAvailableThenItemRetrieved() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(UserMapper.INSTANCE.toUser(user)));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(ItemMapper.INSTANCE.toItem(item)));
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.singletonList(CommentMapper.INSTANCE.toComment(comment)));

        ItemDto result = itemService.getById(user.getId(), item.getId());

        assertNotNull(result);
        assertEquals(itemDto.getName(), result.getName());
        verify(commentRepository, times(1)).findByItemId(anyLong());
    }

    @Test
    void getByIdShouldReturnItemDtoWithCommentsWhenUserIsNotOwner() {
        Long userId = 1L;
        Long ownerId = 2L;
        Long itemId = 1L;
        User user = new User(userId, "User Name", "user@example.com");
        User owner = new User(ownerId, "Owner Name", "owner@example.com");
        Item item = new Item(itemId, "Item Name", "Item Description", true, owner, null);
        Comment comment = new Comment(1L, "Great item!", item, user, LocalDateTime.now());
        CommentDto commentDto = CommentMapper.INSTANCE.toCommentDto(comment);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(itemId)).thenReturn(Collections.singletonList(comment));

        ItemDto result = itemService.getById(userId, itemId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Item Name", result.getName());
        assertEquals("Item Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        assertNotNull(result.getComments());
        assertFalse(result.getComments().isEmpty());
        assertEquals(1, result.getComments().size());
        assertEquals(commentDto.getText(), result.getComments().get(0).getText());

        verify(userRepository).findById(userId);
        verify(itemRepository).findById(itemId);
        verify(commentRepository).findByItemId(itemId);
    }

    @Test
    void testGetAllItemsWhenAllDependenciesAvailableThenAllItemsRetrieved() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(UserMapper.INSTANCE.toUser(user)));
        when(itemRepository.getItemsByOwnerId(anyLong())).thenReturn(Collections.singletonList(ItemMapper.INSTANCE.toItem(item)));
        when(bookingRepository.findLastBookingsForOwnerItems(anyLong())).thenReturn(Collections.singletonList(BookingMapper.INSTANCE.toBookingFromBookingResponseDto(booking)));
        when(bookingRepository.findNextBookingsForOwnerItems(anyLong())).thenReturn(Collections.singletonList(BookingMapper.INSTANCE.toBookingFromBookingResponseDto(booking)));
        when(commentRepository.findByAuthorId(anyLong())).thenReturn(Collections.singletonList(CommentMapper.INSTANCE.toComment(comment)));

        List<ItemDto> result = itemService.getAllItems(user.getId());

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(itemRepository, times(1)).getItemsByOwnerId(anyLong());
    }

    @Test
    void testGetAllItemsWithWrongId() {
        when(itemService.getAllItems(null)).thenReturn(new ArrayList<>());

        List<ItemDto> result = itemService.getAllItems(null);

        assertEquals(result, new ArrayList<>());
    }

    @Test
    void getAllItemsShouldKeepExistingBookingWhenCollisionOccurs() {
        Long ownerId = 1L;
        Long itemId = 1L;
        User booker = new User(1L, "User Name", "user@example.com");
        Item item = new Item(1L, "Item Name", "Item Description", true, booker, null);
        Booking lastBooking = new Booking(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(1), item, booker, BookingStatus.APPROVED);
        Booking anotherLastBooking = new Booking(2L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), item, booker, BookingStatus.APPROVED);

        when(itemRepository.getItemsByOwnerId(ownerId)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findLastBookingsForOwnerItems(ownerId)).thenReturn(Arrays.asList(lastBooking, anotherLastBooking));
        when(bookingRepository.findNextBookingsForOwnerItems(ownerId)).thenReturn(new ArrayList<>());
        when(commentRepository.findByAuthorId(ownerId)).thenReturn(new ArrayList<>());

        List<ItemDto> items = itemService.getAllItems(ownerId);

        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(1, items.size());
        ItemDto resultItem = items.get(0);
        assertNotNull(resultItem.getLastBooking());
        assertEquals(lastBooking.getId(), resultItem.getLastBooking().getId());

        verify(itemRepository).getItemsByOwnerId(ownerId);
        verify(bookingRepository).findLastBookingsForOwnerItems(ownerId);
        verify(bookingRepository).findNextBookingsForOwnerItems(ownerId);
        verify(commentRepository).findByAuthorId(ownerId);
    }

    @Test
    void shouldKeepExistingNextBookingWhenCollisionOccurs() {
        Long ownerId = 1L;
        Long itemId = 1L;
        User owner = new User(ownerId, "Owner Name", "owner@example.com");
        Item item = new Item(itemId, "Item Name", "Item Description", true, owner, null);
        Booking firstNextBooking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item, new User(2L, "User 1", "user1@example.com"), BookingStatus.APPROVED);
        Booking secondNextBooking = new Booking(2L, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), item, new User(3L, "User 2", "user2@example.com"), BookingStatus.APPROVED);

        when(itemRepository.getItemsByOwnerId(ownerId)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findLastBookingsForOwnerItems(ownerId)).thenReturn(new ArrayList<>());
        when(bookingRepository.findNextBookingsForOwnerItems(ownerId)).thenReturn(Arrays.asList(firstNextBooking, secondNextBooking));
        when(commentRepository.findByAuthorId(ownerId)).thenReturn(new ArrayList<>());

        List<ItemDto> items = itemService.getAllItems(ownerId);

        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(1, items.size());
        ItemDto resultItem = items.get(0);
        assertNotNull(resultItem.getNextBooking());
        assertEquals(firstNextBooking.getId(), resultItem.getNextBooking().getId(), "Should keep the first next booking when collision occurs");

        verify(itemRepository).getItemsByOwnerId(ownerId);
        verify(bookingRepository).findLastBookingsForOwnerItems(ownerId);
        verify(bookingRepository).findNextBookingsForOwnerItems(ownerId);
        verify(commentRepository).findByAuthorId(ownerId);
    }

    @Test
    void testSearchByTextWhenAllDependenciesAvailableThenItemsSearched() {
        when(itemRepository.search(anyString())).thenReturn(Collections.singletonList(ItemMapper.INSTANCE.toItem(item)));

        List<ItemDto> result = itemService.searchByText("Item");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(itemRepository, times(1)).search(anyString());
    }

    @Test
    void testSaveCommentWhenAllDependenciesAvailableThenCommentSaved() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(UserMapper.INSTANCE.toUser(user)));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(ItemMapper.INSTANCE.toItem(item)));
        when(bookingRepository.findFinishedBookingsByItemAndUser(anyLong(), anyLong())).thenReturn(Collections.singletonList(BookingMapper.INSTANCE.toBookingFromBookingResponseDto(booking)));
        when(commentRepository.save(any(Comment.class))).thenReturn(CommentMapper.INSTANCE.toComment(comment));

        CommentDto result = itemService.saveComment(item.getId(), user.getId(), commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getText(), result.getText());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void saveCommentShouldThrowCustomBadRequestExceptionWhenNoFinishedBookings() {
        Long itemId = 1L;
        Long userId = 1L;
        CommentDto commentDto = new CommentDto();

        User user = new User(userId, "User Name", "user@example.com");
        Item item = new Item(itemId, "Item Name", "Item Description", true, user, null);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(itemRepository.findById(itemId)).thenReturn(java.util.Optional.of(item));
        when(bookingRepository.findFinishedBookingsByItemAndUser(itemId, userId)).thenReturn(Collections.emptyList());

        assertThrows(CustomBadRequestException.class, () -> itemService.saveComment(itemId, userId, commentDto),
                "User cant comment this item, cause booking isn't done already");
    }
}