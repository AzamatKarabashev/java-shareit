package ru.practicum.shareit.item.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@WebMvcTest(ItemGatewayController.class)
public class ItemGatewayControllerTest {

    public static final String USER_ID = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @BeforeEach
    public void setUp() {
        Mockito.when(itemClient.saveItem(Mockito.anyLong(), Mockito.any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());
        Mockito.when(itemClient.updateItem(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok().build());
        Mockito.when(itemClient.getItem(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(ResponseEntity.ok().build());
        Mockito.when(itemClient.getItems(Mockito.anyLong()))
                .thenReturn(ResponseEntity.ok().build());
        Mockito.when(itemClient.searchByText(Mockito.anyString()))
                .thenReturn(ResponseEntity.ok().build());
        Mockito.when(itemClient.saveComment(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok().build());
    }

    @Test
    public void testSaveItemWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content("{\"name\":\"Item\",\"description\":\"Description\",\"available\":true}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUpdateItemWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.patch("/items/1")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content("{\"name\":\"Updated Item\",\"description\":\"Updated Description\",\"available\":false}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetItemWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/items/1")
                        .header(USER_ID, 1L))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetItemsWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/items")
                        .header(USER_ID, 1L))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testSearchByTextWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/items/search")
                        .param("text", "Item"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testSaveCommentWhenValidThenReturnOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/items/1/comment")
                        .header(USER_ID, 1L)
                        .contentType("application/json")
                        .content("{\"text\":\"Great item!\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}