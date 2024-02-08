package ru.practicum.shareit.item.repository.api;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item saveItem(Item item);

    Optional<Item> getById(Long id);

    Item updateItem(Long id, Item item);

    List<Item> getAllItems();

    List<Item> getItemsByOwnerId(Long id);

    List<Item> searchByText(String text);
}