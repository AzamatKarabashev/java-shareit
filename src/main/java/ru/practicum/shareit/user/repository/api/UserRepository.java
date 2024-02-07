package ru.practicum.shareit.user.repository.api;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> getById(Long id);

    User saveUser(User user);

    Boolean isEmailAlreadyExist(String email);

    User updateUser(Long id, User user);

    void deleteUserById(Long id);

    List<User> getAllUsers();
}