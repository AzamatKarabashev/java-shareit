package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.api.BookingService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSaveBookingWhenCalledWithValidParametersThenReturnBookingResponseDto() throws Exception {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(requestDto.getStart())
                .end(requestDto.getEnd())
                .build();
        when(bookingService.saveBooking(eq(1L), any(BookingRequestDto.class))).thenReturn(responseDto);

        String jsonFromDto = mapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/bookings")
                        .header(BookingController.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFromDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));
    }

    @Test
    void testUpdateBookingWhenCalledWithValidParametersThenReturnBookingResponseDto() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        when(bookingService.updateBooking(eq(1L), eq(1L), eq(true))).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header(BookingController.USER_ID, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));
    }

    @Test
    void testGetBookingByBookingIdWhenCalledWithValidParametersThenReturnBookingResponseDto() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        when(bookingService.getBookingByBookingId(eq(1L), eq(1L))).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header(BookingController.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));
    }

    @Test
    void testGetBookingByBookerIdWhenCalledWithValidParametersThenReturnListOfBookingResponseDto() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        List<BookingResponseDto> responseDtoList = Collections.singletonList(responseDto);

        when(bookingService.getBookingByBookerId(eq(1L), eq("ALL"), eq(0), eq(10))).thenReturn(responseDtoList);

        mockMvc.perform(get("/bookings")
                        .header(BookingController.USER_ID, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()));
    }

    @Test
    void testGetBookingByOwnerIdWhenCalledWithValidParametersThenReturnListOfBookingResponseDto() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
        List<BookingResponseDto> responseDtoList = Collections.singletonList(responseDto);

        when(bookingService.getBookingByOwnerId(eq(1L), eq("ALL"), eq(0), eq(10))).thenReturn(responseDtoList);

        mockMvc.perform(get("/bookings/owner")
                        .header(BookingController.USER_ID, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()));
    }
}
