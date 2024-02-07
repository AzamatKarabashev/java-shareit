package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.api.UserService;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {

    private final UserService service;

    @PostMapping
    public UserDto saveUser(@Validated(OnCreate.class) @RequestBody UserDto userDto) {
        log.debug("POST request received to save user");
        return service.saveUser(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @Validated(OnUpdate.class) @RequestBody UserDto userDto) {
        log.debug("PUT request received to update user by id={}", userDto.getId());
        return service.updateUser(id, userDto);
    }

    @GetMapping("{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.debug("GET request received to get user by id={}", id);
        return service.getUserById(id);
    }

    @DeleteMapping("{id}")
    public void deleteUserById(@PathVariable Long id) {
        log.debug("DELETE request received to delete user by id={}", id);
        service.deleteUserById(id);
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        log.debug("GET request received to get all users");
        return service.getAllUsers();
    }
}