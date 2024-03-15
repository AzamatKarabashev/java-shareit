package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingItemDto;
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
import ru.practicum.shareit.item.service.api.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto saveItem(Long id, ItemDto itemDto) {
        log.debug("saveItem method called in Service to save");
        User owner = userRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Owner not exist"));
        Item item = ItemMapper.INSTANCE.toItem(itemDto);
        item.setOwner(owner);
        Long requestId = item.getRequestId();
        if (requestId != null) {
            item.setRequestId(requestId);
        }
        item = itemRepository.save(item);
        return ItemMapper.INSTANCE.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long id, Long itemId, ItemDto itemDto) {
        log.debug("updateItem method was called in Service to update");
        User existOwner = userRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Owner not exist"));
        Item existItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomEntityNotFoundException("Item not exist"));
        if (!existItem.getOwner().getId().equals(existOwner.getId())) {
            throw new CustomEntityNotFoundException("Owner not exist");
        }
        Item save = ItemMapper.INSTANCE.updateItemByGivenDto(itemDto, existItem);
        Item result = itemRepository.save(save);
        return ItemMapper.INSTANCE.toItemDto(result);
    }

    @Override
    public ItemDto getById(Long id, Long itemId) {
        log.debug("getById method was called in Service to get item");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Owner not exist"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomEntityNotFoundException("Item not exist"));
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper.INSTANCE::toCommentDto)
                .collect(Collectors.toList());
        if (Objects.equals(item.getOwner().getId(), user.getId())) {
            ItemDto itemWithBookings = getItemWithBookings(itemId);
            itemWithBookings.setComments(comments);
            return itemWithBookings;
        }
        ItemDto itemDto = ItemMapper.INSTANCE.toItemDto(item);
        itemDto.setComments(comments);
        return itemDto;
    }

    @Override
    public List<ItemDto> getAllItems(Long id) {
        List<Item> items = itemRepository.getItemsByOwnerId(id);
        List<Booking> lastBookings = bookingRepository.findLastBookingsForOwnerItems(id);
        List<Booking> nextBookings = bookingRepository.findNextBookingsForOwnerItems(id);
        if (id != null) {
            Map<Long, BookingItemDto> lastBookingsMap = lastBookings.stream()
                    .collect(Collectors.toMap(
                            booking -> booking.getItem().getId(),
                            this::toBookingItemDto,
                            (existing, replacement) -> existing
                    ));
            Map<Long, BookingItemDto> nextBookingsMap = nextBookings.stream()
                    .collect(Collectors.toMap(
                            booking -> booking.getItem().getId(),
                            this::toBookingItemDto,
                            (existing, replacement) -> existing
                    ));
            List<CommentDto> comments = commentRepository.findByAuthorId(id).stream()
                    .map(CommentMapper.INSTANCE::toCommentDto)
                    .collect(Collectors.toList());
            return items.stream()
                    .map(item -> ItemDto.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .available(item.getAvailable())
                            .lastBooking(lastBookingsMap.get(item.getId()))
                            .nextBooking(nextBookingsMap.get(item.getId()))
                            .comments(comments)
                            .build())
                    .sorted(Comparator.comparing(ItemDto::getId))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public List<ItemDto> searchByText(String text) {
        return itemRepository.search(text).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper.INSTANCE::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto saveComment(Long itemId, Long userId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomEntityNotFoundException("User not exist"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomEntityNotFoundException("Item not exist"));
        List<Booking> finishedBookings = bookingRepository.findFinishedBookingsByItemAndUser(itemId, userId);
        if (finishedBookings.isEmpty()) {
            throw new CustomBadRequestException("User cant comment this item, cause booking isn't done already");
        }
        Comment comment = CommentMapper.INSTANCE.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.INSTANCE.toCommentDto(savedComment);
    }

    private ItemDto getItemWithBookings(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new CustomEntityNotFoundException("Item not found"));
        BookingItemDto lastBookingDto = findPastBookingsByItemId(itemId);
        BookingItemDto nextBookingDto = findFutureBookingsByItemId(itemId);
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBookingDto)
                .nextBooking(nextBookingDto)
                .build();
    }

    private BookingItemDto findFutureBookingsByItemId(Long itemId) {
        return bookingRepository.findFutureBookingsByItemId(itemId)
                .stream()
                .map(this::toBookingItemDto)
                .findFirst()
                .orElse(null);
    }

    private BookingItemDto findPastBookingsByItemId(Long itemId) {
        return bookingRepository.findPastBookingsByItemId(itemId)
                .stream()
                .map(this::toBookingItemDto)
                .findFirst()
                .orElse(null);
    }

    private BookingItemDto toBookingItemDto(Booking booking) {
        return new BookingItemDto(booking.getId(), booking.getBooker().getId());
    }
}