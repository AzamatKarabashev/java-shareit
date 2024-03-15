package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.api.ItemService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
    }

    @Test
    void testSaveItemWhenCalledWithValidParametersThenReturnItemDto() throws Exception {
        ItemDto dto = ItemDto.builder()
                .name("Созданный предмет")
                .description("Описание созданного предмета")
                .available(true)
                .lastBooking(new BookingItemDto(1L, 1L))
                .nextBooking(new BookingItemDto(2L, 2L))
                .comments(new ArrayList<>())
                .requestId(3L)
                .build();
        ItemDto responseDto = ItemDto.builder()
                .id(1L)
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .lastBooking(dto.getLastBooking())
                .nextBooking(dto.getNextBooking())
                .comments(dto.getComments())
                .requestId(dto.getRequestId())
                .build();
        String jsonFromDto = mapper.writeValueAsString(dto);

        when(itemService.saveItem(eq(1L), any(ItemDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header(ItemController.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFromDto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()));
    }

    @Test
    void testUpdateItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Обновленный предмет")
                .description("Описание обновленного предмета")
                .available(true)
                .build();
        String itemDtoJson = mapper.writeValueAsString(itemDto);

        when(itemService.updateItem(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header(ItemController.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemDtoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    void testGetById() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Найденный предмет")
                .description("Описание найденного предмета")
                .available(true)
                .build();
        when(itemService.getById(1L, 1L)).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header(ItemController.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()));
    }

    @Test
    void testGetAllItems() throws Exception {
        List<ItemDto> items = List.of(
                ItemDto.builder()
                        .id(1L)
                        .name("Первый предмет")
                        .description("Описание первого предмета")
                        .available(true)
                        .build(),
                ItemDto.builder()
                        .id(2L)
                        .name("Второй предмет")
                        .description("Описание второго предмета")
                        .available(false)
                        .build()
        );
        when(itemService.getAllItems(1L)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header(ItemController.USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(items.size())))
                .andExpect(jsonPath("$[0].id").value(items.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(items.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(items.get(0).getDescription()))
                .andExpect(jsonPath("$[0].available").value(items.get(0).getAvailable()))
                .andExpect(jsonPath("$[1].id").value(items.get(1).getId()))
                .andExpect(jsonPath("$[1].name").value(items.get(1).getName()))
                .andExpect(jsonPath("$[1].description").value(items.get(1).getDescription()))
                .andExpect(jsonPath("$[1].available").value(items.get(1).getAvailable()));
    }

    @Test
    void testSearchByText() throws Exception {
        List<ItemDto> items = List.of(
                ItemDto.builder()
                        .id(1L)
                        .name("Предмет с текстом")
                        .description("Описание первого предмета содержит text")
                        .available(true)
                        .build(),
                ItemDto.builder()
                        .id(2L)
                        .name("Еще один предмет")
                        .description("Описание второго предмета также содержит text")
                        .available(false)
                        .build()
        );
        when(itemService.searchByText("text")).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", "text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(items.size())))
                .andExpect(jsonPath("$[0].id").value(items.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(items.get(0).getName()))
                .andExpect(jsonPath("$[0].description").value(items.get(0).getDescription()))
                .andExpect(jsonPath("$[0].available").value(items.get(0).getAvailable()))
                .andExpect(jsonPath("$[1].id").value(items.get(1).getId()))
                .andExpect(jsonPath("$[1].name").value(items.get(1).getName()))
                .andExpect(jsonPath("$[1].description").value(items.get(1).getDescription()))
                .andExpect(jsonPath("$[1].available").value(items.get(1).getAvailable()));
    }

    @Test
    void testSearchByTextWithNoResults() throws Exception {
        when(itemService.searchByText("несуществующий текст")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/items/search")
                        .param("text", "несуществующий текст"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testSearchByTextWhenTextIsNull() throws Exception {
        mockMvc.perform(get("/items/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testSaveComment() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .text("Отличный предмет")
                .build();
        CommentDto responseDto = CommentDto.builder()
                .id(1L)
                .text(commentDto.getText())
                .authorName("Иван Иванов")
                .created(LocalDateTime.now())
                .build();
        String commentDtoJson = mapper.writeValueAsString(commentDto);

        when(itemService.saveComment(eq(1L), eq(1L), any(CommentDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(ItemController.USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentDtoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.text").value(responseDto.getText()))
                .andExpect(jsonPath("$.authorName").value(responseDto.getAuthorName()))
                .andExpect(jsonPath("$.created").exists());
    }
}