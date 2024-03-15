package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.api.ItemService;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    public static final String USER_ID = "X-Sharer-User-Id";

    private final ItemService service;

    @PostMapping
    public ItemDto saveItem(@RequestHeader(USER_ID) Long id,
                            @RequestBody ItemDto itemDto) {
        log.debug("POST request received to save item");
        return service.saveItem(id, itemDto);
    }

    @PatchMapping("{itemId}")
    public ItemDto updateItem(@RequestHeader(USER_ID) Long id,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        log.debug("PATCH request received to update item with id={}", itemId);
        return service.updateItem(id, itemId, itemDto);
    }

    @GetMapping("{itemId}")
    public ItemDto getById(@RequestHeader(USER_ID) Long id,
                           @PathVariable Long itemId) {
        log.debug("GET request received to get item by id={}", itemId);
        return service.getById(id, itemId);
    }

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(USER_ID) Long id) {
        log.debug("GET request received to get list of items");
        return service.getAllItems(id);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByText(@RequestParam(required = false) String text) {
        log.debug("GET request received to search by text: '{}'", text);
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return service.searchByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto saveComment(@PathVariable Long itemId,
                                  @RequestHeader(USER_ID) Long userId,
                                  @RequestBody CommentDto commentDto) {
        return service.saveComment(itemId, userId, commentDto);
    }
}