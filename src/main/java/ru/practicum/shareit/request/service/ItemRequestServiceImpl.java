package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.api.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.api.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto saveRequest(Long requestorId, ItemRequestDto dto) {
        log.debug("saveRequest method was called in ItemRequestServiceImpl");
        User user = userRepository.findById(requestorId).orElseThrow(() -> new CustomEntityNotFoundException("User not exist"));
        ItemRequest itemRequest = ItemRequestMapper.INSTANCE.toItemRequest(dto);
        itemRequest.setRequestor(user);
        ItemRequest savedRequest = requestRepository.save(itemRequest);
        return ItemRequestMapper.INSTANCE.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getRequests(Long requestorId) {
        log.debug("getRequests method was called in ItemRequestServiceIml ");
        userRepository.findById(requestorId).orElseThrow(() -> new CustomEntityNotFoundException("User not exist"));
        List<ItemRequest> itemRequestsByRequestorId = requestRepository.findItemRequestsByRequestorId(requestorId);
        return itemRequestsByRequestorId.stream()
                .map(this::convertToItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getRequestByPagination(Long requestorId, Integer from, Integer size) {
        log.debug("getRequestByPagination method was called in ItemRequestServiceIml");
        userRepository.findById(requestorId).orElseThrow(() -> new CustomEntityNotFoundException("User not exist"));
        if (from != null && size != null) {
            Pageable pageable = PageRequest.of(from / size, size);
            List<ItemRequest> itemRequests = requestRepository.findItemRequestsByRequestorId(requestorId, pageable).getContent();
            return itemRequests.stream()
                    .map(this::convertToItemRequestDtoWithItems)
                    .collect(Collectors.toList());
        }
        return getRequests(requestorId);
    }

    @Override
    public ItemRequestDto getRequestById(Long requestorId, Long requestId) {
        userRepository.findById(requestorId).orElseThrow(() -> new CustomEntityNotFoundException("User not exist"));
        ItemRequest itemRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new CustomEntityNotFoundException("Request not exist"));
        return convertToItemRequestDtoWithItems(itemRequest);
    }

    private ItemRequestDto convertToItemRequestDtoWithItems(ItemRequest itemRequest) {
        ItemRequestDto dto = ItemRequestMapper.INSTANCE.toItemRequestDto(itemRequest);
        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());
        List<ItemDto> itemDtoList = items.stream()
                .map(ItemMapper.INSTANCE::toItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtoList);
        return dto;
    }
}