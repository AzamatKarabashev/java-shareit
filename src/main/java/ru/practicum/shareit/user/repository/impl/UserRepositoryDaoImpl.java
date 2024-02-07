package ru.practicum.shareit.user.repository.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepositoryDaoImpl implements UserRepository {

    private final AtomicLong idGenerator = new AtomicLong(0);

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Boolean isEmailAlreadyExist(String email) {
        return email != null && !email.isEmpty() && users.values().stream()
                .anyMatch(user -> email.equals(user.getEmail()));
    }

    @Override
    public Optional<User> getById(Long id) {
        if (!users.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(users.get(id));
    }

    @Override
    public User saveUser(User user) {
        user.setId(idGenerator.incrementAndGet());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(Long id, User user) {
        User result = users.get(id);
        return getUser(id, user, result);
    }

    @Override
    public void deleteUserById(Long id) {
        try {
            users.remove(id);
        } catch (RuntimeException e) {
            throw new CustomEntityNotFoundException("cant delete user cause user not exist");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private User getUser(Long id, User user, User result) {
        if (result != null) {
            if (user.getName() != null) {
                result.setName(user.getName());
            }
            if (user.getEmail() != null) {
                result.setEmail(user.getEmail());
            }
            users.put(id, result);
            return result;
        } else {
            throw new CustomEntityNotFoundException("User not exist");
        }
    }
}
