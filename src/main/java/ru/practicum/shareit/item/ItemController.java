package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.api.ItemService;
import ru.practicum.shareit.validation.OnCreate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    public static final String OWNER_ID = "X-Sharer-User-Id";

    private final ItemService service;

    @PostMapping
    public ItemDto saveItem(@RequestHeader(OWNER_ID) Long id,
                            @Validated(OnCreate.class) @RequestBody ItemDto itemDto) {
        log.debug("POST request received to save item");
        return service.saveItem(id, itemDto);
    }

    @PatchMapping("{itemId}")
    public ItemDto updateItem(@RequestHeader(OWNER_ID) Long id,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        log.debug("PATCH request received to update item with id={}", itemId);
        return service.updateItem(id, itemId, itemDto);
    }

    @GetMapping("{itemId}")
    public ItemDto getById(@RequestHeader(OWNER_ID) Long id,
                           @PathVariable Long itemId) {
        log.debug("GET request received to get item by id={}", itemId);
        return service.getById(id, itemId);
    }

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(OWNER_ID) Long id) {
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
}