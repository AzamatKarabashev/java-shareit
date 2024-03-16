package ru.practicum.shareit.request.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@WebMvcTest(ItemRequestGatewayController.class)
public class ItemRequestGatewayControllerTest {

    public static final String USER_ID = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @BeforeEach
    public void setUp() {
        Mockito.when(itemRequestClient.saveRequest(Mockito.anyLong(), Mockito.any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok().build());
        Mockito.when(itemRequestClient.getRequests(Mockito.anyLong()))
                .thenReturn(ResponseEntity.ok().build());
        Mockito.when(itemRequestClient.getRequestsByPagination(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(ResponseEntity.ok().build());
        Mockito.when(itemRequestClient.getRequestById(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(ResponseEntity.ok().build());
    }

    @Test
    public void testSaveRequestWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/requests")
                        .header(USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need a drill\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetRequestsWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/requests")
                        .header(USER_ID, 1L))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetRequestsByPaginationWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/requests/all")
                        .header(USER_ID, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetRequestByIdWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/requests/1")
                        .header(USER_ID, 1L))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}