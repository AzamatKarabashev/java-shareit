package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.api.UserService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService service;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(service))
                .build();
    }

    @Test
    void testSaveUserWhenValidUserThenReturnSavedUser() throws Exception {
        UserDto userDto = UserDto.builder().name("Test User").email("test@example.com").build();
        UserDto savedUserDto = UserDto.builder().id(1L).name("Test User").email("test@example.com").build();

        when(service.saveUser(any(UserDto.class))).thenReturn(savedUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(savedUserDto)));

        verify(service, times(1)).saveUser(any(UserDto.class));
    }

    @Test
    void testUpdateUserWhenValidUserThenReturnUpdatedUser() throws Exception {
        Long userId = 1L;
        UserDto userDto = UserDto.builder().name("Updated User").email("updated@example.com").build();
        UserDto updatedUserDto = UserDto.builder().id(userId).name("Updated User").email("updated@example.com").build();

        when(service.updateUser(eq(userId), any(UserDto.class))).thenReturn(updatedUserDto);

        mockMvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(updatedUserDto)));

        verify(service, times(1)).updateUser(eq(userId), any(UserDto.class));
    }

    @Test
    void testGetUserByIdWhenValidIdThenReturnUser() throws Exception {
        Long userId = 1L;
        UserDto userDto = UserDto.builder().id(userId).name("Test User").email("test@example.com").build();

        when(service.getUserById(userId)).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userDto)));

        verify(service, times(1)).getUserById(userId);
    }

    @Test
    void testDeleteUserByIdWhenValidIdThenReturnNoContent() throws Exception {
        Long userId = 1L;

        doNothing().when(service).deleteUserById(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());

        verify(service, times(1)).deleteUserById(userId);
    }

    @Test
    void testGetAllUsersThenReturnListOfUsers() throws Exception {
        List<UserDto> users = Arrays.asList(
                UserDto.builder().id(1L).name("Test User 1").email("user1@example.com").build(),
                UserDto.builder().id(2L).name("Test User 2").email("user2@example.com").build()
        );

        when(service.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(users)));

        verify(service, times(1)).getAllUsers();
    }
}
