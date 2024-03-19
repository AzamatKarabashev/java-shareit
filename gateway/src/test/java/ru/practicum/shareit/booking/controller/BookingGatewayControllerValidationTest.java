package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BookingGatewayController.class)
public class BookingGatewayControllerValidationTest {

    public static final String USER_ID = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    BookingRequestDto validDto;

    @BeforeEach
    void setUp() {
        LocalDateTime timeStamp = LocalDateTime.now();
        validDto = BookingRequestDto.builder()
                .start(timeStamp.plusDays(1))
                .end(timeStamp.plusDays(2))
                .itemId(1L)
                .build();
    }

    @Test
    void whenGetRequestToBookingsWithWrongState_thenThrowBadRequestException() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID, 1)
                        .param("state", "INVALID_STATE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetBookingsWithInvalidPagination_thenExpectBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_ID, 1)
                        .param("from", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID, 1)
                        .param("from", "0")
                        .param("size", "-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenBookItemWithInvalidStart_thenExpectBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        validDto.setStart(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenBookItemWithInvalidEnd_thenExpectBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        validDto.setEnd(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenBookItemWithInvalidStartBeforeEnd_thenExpectBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        validDto.setStart(LocalDateTime.now().plusDays(3));
        validDto.setEnd(LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenBookItemWithInvalidNullStart_thenExpectBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        validDto.setStart(null);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenBookItemWithInvalidNullEnd_thenExpectBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        validDto.setEnd(null);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenBookItemWithInvalidNullItemId_thenExpectBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        validDto.setItemId(null);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID, 1)
                        .content(objectMapper.writeValueAsString(validDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}