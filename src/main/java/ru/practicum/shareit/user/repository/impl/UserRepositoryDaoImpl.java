package ru.practicum.shareit.user.repository.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepositoryDaoImpl implements UserRepository {

    private AtomicLong idGenerator = new AtomicLong(0);

    private Map<Long, User> users = new HashMap<>();

    @Override
    public Boolean isEmailAlreadyExist(String email) {
        if (email == null) {
            return false;
        }
        for (User value : users.values()) {
            if (email.equals(value.getEmail())) {
                return true;
            }
        }
        return false;
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
            throw new EntityNotFoundException("User not exist");
        }
    }

    @Override
    public void deleteUserById(Long id) {
        try {
            users.remove(id);
        } catch (RuntimeException e) {
            throw new EntityNotFoundException("cant delete user cause user not exist");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}