package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestGatewayController {

    public static final String USER_ID = "X-Sharer-User-Id";

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> saveRequest(@RequestHeader(USER_ID) Long requestorId,
                                              @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestClient.saveRequest(requestorId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getRequests(@RequestHeader(USER_ID) Long requestorId) {
        return itemRequestClient.getRequests(requestorId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getRequestsByPagination(@RequestHeader(USER_ID) Long requestorId,
                                                          @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                          @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemRequestClient.getRequestsByPagination(requestorId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(USER_ID) Long requestorId,
                                                 @PathVariable Long requestId) {
        return itemRequestClient.getRequestById(requestorId, requestId);
    }
}
