package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.api.ItemRepository;
import ru.practicum.shareit.item.service.api.ItemService;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.mapper.ItemMapper.toItem;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto saveItem(Long id, ItemDto itemDto) {
        log.debug("saveItem method called in Service to save");
        if (userRepository.getById(id).isEmpty()) {
            throw new CustomEntityNotFoundException("Owner not exist");
        }
        Item item = toItem(itemDto);
        item.setOwner(userRepository.getById(id).get());
        item = itemRepository.saveItem(item);
        return toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long id, Long itemId, ItemDto itemDto) {
        log.debug("updateItem method was called in Service to update");
        userRepository.getById(id).orElseThrow(() -> new CustomEntityNotFoundException("Owner not exist"));
        Item itemToUpdate = itemRepository.getById(itemId)
                .orElseThrow(() -> new CustomEntityNotFoundException("Item not exist"));
        if (!itemToUpdate.getOwner().getId().equals(id)) {
            throw new CustomEntityNotFoundException("Owner not exist");
        }
        itemToUpdate = toItem(itemDto);
        Item updatedItem = itemRepository.updateItem(itemId, itemToUpdate);
        return toItemDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long id, Long itemId) {
        log.debug("getById method was called in Service to get item");
        userRepository.getById(id)
                .orElseThrow(() -> new CustomEntityNotFoundException("Owner not exist"));
        Item itemToReceive = itemRepository.getById(itemId)
                .orElseThrow(() -> new CustomEntityNotFoundException("Item not exist"));
        return toItemDto(itemToReceive);
    }

    @Override
    public List<ItemDto> getAllItems(Long id) {
        if (id != null && userRepository.getById(id).isPresent()) {
            return itemRepository.getItemsByOwnerId(id).stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
        return itemRepository.getAllItems().stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchByText(String text) {
        return itemRepository.searchByText(text).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}