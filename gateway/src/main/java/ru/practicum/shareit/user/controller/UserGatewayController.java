package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserGatewayController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> saveUser(@Validated(OnCreate.class) @RequestBody UserDto userDto) {
        return userClient.saveUser(userDto);
    }

    @PatchMapping(path = "/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable Long userId, @Validated(OnUpdate.class) @RequestBody UserDto userDto) {
        return userClient.updateUser(userId, userDto);
    }

    @GetMapping(path = "/{userId}")
    public ResponseEntity<Object> getUser(@PathVariable Long userId) {
        return userClient.getUser(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        return userClient.getUsers();
    }

    @DeleteMapping(path = "/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long userId) {
        userClient.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}