package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.api.UserService;
import ru.practicum.shareit.user.repository.api.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.user.mapper.UserMapper.toUser;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public UserDto saveUser(UserDto userDto) {
        log.debug("createUser method was called in Service to createUser with name={}", userDto.getName());
        if (repository.isEmailAlreadyExist(userDto.getEmail())) {
            throw new ConflictException("Email already exist");
        } else {
            User user = toUser(userDto);
            user = repository.saveUser(user);
            return toUserDto(user);
        }
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.debug("updateUser method in service was called");
        Optional<User> byId = repository.getById(id);
        if (byId.orElseThrow().getEmail().equals(userDto.getEmail())) {
            return toUserDto(byId.get());
        }
        if (repository.isEmailAlreadyExist(userDto.getEmail())) {
            throw new ConflictException("Email already exist");
        }
        User user = toUser(userDto);
        user = repository.updateUser(id, user);
        return toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long id) {
        log.debug("getUserById method was called in service");
        return toUserDto(repository.getById(id).orElseThrow());
    }

    @Override
    public void deleteUserById(Long id) {
        log.debug("deleteUserById method was called in service");
        repository.deleteUserById(id);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return repository.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}