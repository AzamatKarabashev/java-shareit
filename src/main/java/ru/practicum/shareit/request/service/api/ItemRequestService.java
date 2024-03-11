package ru.practicum.shareit.request.service.api;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto saveRequest(Long requestorId, ItemRequestDto dto);

    List<ItemRequestDto> getRequests(Long requestorId);

    List<ItemRequestDto> getRequestByPagination(Long requestorId, Integer from, Integer size);

    ItemRequestDto getRequestById(Long requestorId, Long requestId);
}
