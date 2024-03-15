package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.CustomEntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.api.UserRepository;
import ru.practicum.shareit.user.service.api.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public UserDto saveUser(UserDto userDto) {
        User user = UserMapper.INSTANCE.toUser(userDto);
        user = repository.save(user);
        return UserMapper.INSTANCE.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.debug("updateUser method in service was called");
        User existUser = repository.findById(userId).orElseThrow(() -> new CustomEntityNotFoundException("User not exist"));
        User result = UserMapper.INSTANCE.updateUserByGivenDto(userDto, existUser);
        User save = repository.save(result);
        return UserMapper.INSTANCE.toUserDto(save);
    }

    @Override
    public UserDto getUserById(Long id) {
        log.debug("getUserById method was called in service");
        return UserMapper.INSTANCE.toUserDto(repository.findById(id).orElseThrow(() -> new CustomEntityNotFoundException("User not exist")));
    }

    @Override
    public void deleteUserById(Long id) {
        log.debug("deleteUserById method was called in service");
        repository.deleteById(id);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return repository.findAll().stream()
                .map(UserMapper.INSTANCE::toUserDto)
                .collect(Collectors.toList());
    }
}