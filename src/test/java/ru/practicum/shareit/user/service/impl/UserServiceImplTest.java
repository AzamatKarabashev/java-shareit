package ru.practicum.shareit.user.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    @DisplayName("testSaveUserWhenUserSavedThenReturnUserDto")
    void testSaveUserWhenUserSavedThenReturnUserDto() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.saveUser(userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo(user.getName());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("testUpdateUserWhenUserUpdatedThenReturnUserDto")
    void testUpdateUserWhenUserUpdatedThenReturnUserDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.updateUser(1L, userDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo(user.getName());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("testGetUserByIdWhenUserExistsThenReturnUserDto")
    void testGetUserByIdWhenUserExistsThenReturnUserDto() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo(user.getName());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("testDeleteUserByIdWhenUserExistsThenUserDeleted")
    void testDeleteUserByIdWhenUserExistsThenUserDeleted() {
        doNothing().when(userRepository).deleteById(anyLong());

        userService.deleteUserById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("testGetAllUsersWhenUsersExistThenReturnListOfUserDto")
    void testGetAllUsersWhenUsersExistThenReturnListOfUserDto() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(user.getId());
        assertThat(result.get(0).getName()).isEqualTo(user.getName());
        assertThat(result.get(0).getEmail()).isEqualTo(user.getEmail());
    }
}
