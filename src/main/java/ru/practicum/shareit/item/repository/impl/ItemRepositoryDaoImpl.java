package ru.practicum.shareit.item.repository.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.api.ItemRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ItemRepositoryDaoImpl implements ItemRepository {

    private final AtomicLong idGenerator = new AtomicLong(0);

    private final Map<Long, Item> items = new HashMap<>();

    @Override
    public Item saveItem(Item item) {
        item.setId(idGenerator.incrementAndGet());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> getById(Long id) {
        if (!items.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(items.get(id));
    }

    @Override
    public Item updateItem(Long id, Item item) {
        Item result = items.get(id);
        return getItem(id, item, result);
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> getItemsByOwnerId(Long id) {
        List<Item> result = new ArrayList<>();
        for (Item value : items.values()) {
            if (value.getOwner().getId().equals(id)) {
                result.add(value);
            }
        }
        return result;
    }

    @Override
    public List<Item> searchByText(String text) {
        List<Item> result = new ArrayList<>();
        for (Item value : items.values()) {
            if (value.getDescription().toLowerCase().contains(text.toLowerCase())) {
                result.add(value);
            }
        }
        return result;
    }

    private Item getItem(Long id, Item item, Item result) {
        if (result != null) {
            if (item.getName() != null) {
                result.setName(item.getName());
            }
            if (item.getDescription() != null) {
                result.setDescription(item.getDescription());
            }
            if (item.getAvailable() != null) {
                result.setAvailable(item.getAvailable());
            }
            items.put(id, result);
            return result;
        } else {
            throw new CustomEntityNotFoundException("Item not exist");
        }
    }
}
