package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.item.repository.api.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.api.RequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemRequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "User", "user@example.com");
        itemRequest = new ItemRequest(1L, "Need a drill", user, LocalDateTime.now());
        itemRequestDto = new ItemRequestDto(1L, "Need a drill", 1L, LocalDateTime.now(), Collections.emptyList());

        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(itemRepository.findByRequestId(any(Long.class))).thenReturn(Collections.emptyList());
    }

    @Test
    void saveRequestShouldReturnSavedRequest() {
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.saveRequest(1L, itemRequestDto);

        assertNotNull(result);
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void getRequestsShouldReturnListOfRequests() {
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.findItemRequestsByRequestorId(any(Long.class))).thenReturn(List.of(itemRequest));

        List<ItemRequestDto> result = itemRequestService.getRequests(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).findItemRequestsByRequestorId(any(Long.class));
    }

    @Test
    void getRequestByPaginationShouldReturnListOfRequests() {
        Page<ItemRequest> page = new PageImpl<>(List.of(itemRequest));
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.findItemRequestsByRequestorId(any(Long.class), any(PageRequest.class))).thenReturn(page);

        List<ItemRequestDto> result = itemRequestService.getRequestByPagination(1L, 0, 1);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).findItemRequestsByRequestorId(any(Long.class), any(PageRequest.class));
    }

    @Test
    void getRequestByPaginationShouldReturnGetRequestsResult() {
        when(requestRepository.findItemRequestsByRequestorId(any(Long.class), any())).thenReturn(null);

        List<ItemRequestDto> result = itemRequestService.getRequestByPagination(1L, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getRequestByIdShouldReturnRequest() {
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(itemRequest));

        ItemRequestDto result = itemRequestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).findById(any(Long.class));
    }

    @Test
    void getRequestByIdShouldThrowExceptionWhenRequestNotFound() {
        when(userRepository.findById(any(Long.class))).thenReturn(java.util.Optional.of(user));
        when(requestRepository.findById(any(Long.class))).thenReturn(java.util.Optional.empty());

        assertThrows(CustomEntityNotFoundException.class, () -> itemRequestService.getRequestById(1L, 1L));

        verify(userRepository, times(1)).findById(any(Long.class));
        verify(requestRepository, times(1)).findById(any(Long.class));
    }
}