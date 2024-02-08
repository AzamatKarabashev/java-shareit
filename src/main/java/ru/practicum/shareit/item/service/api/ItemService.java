package ru.practicum.shareit.item.service.api;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto saveItem(Long id, ItemDto itemDto);

    ItemDto updateItem(Long id, Long itemId, ItemDto itemDto);

    ItemDto getById(Long id, Long itemId);

    List<ItemDto> getAllItems(Long id);

    List<ItemDto> searchByText(String text);
}