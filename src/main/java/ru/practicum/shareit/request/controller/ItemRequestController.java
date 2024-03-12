package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.api.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    public static final String USER_ID = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto saveRequest(@RequestHeader(USER_ID) Long requestorId,
                                      @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.saveRequest(requestorId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getRequests(@RequestHeader(USER_ID) Long requestorId) {
        return itemRequestService.getRequests(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getRequestsByPagination(@RequestHeader(USER_ID) Long requestorId,
                                                        @RequestParam(required = false) @PositiveOrZero Integer from,
                                                        @RequestParam(required = false) @Positive Integer size) {
        return itemRequestService.getRequestByPagination(requestorId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@RequestHeader(USER_ID) Long requestorId,
                                         @PathVariable Long requestId) {
        return itemRequestService.getRequestById(requestorId, requestId);
    }
}