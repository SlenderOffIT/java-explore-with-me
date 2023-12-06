package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.users.dto.NewUserRequestDto;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.dto.mapper.UserMapper;
import ru.practicum.users.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> get(List<Long> ids, int from, int size) {
        log.info("Обрабатываем запрос на получение информации о пользователях {}", ids);

        List<UserDto> users;
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        if (ids == null) {
            log.info("Выводим всех пользователей");
            users = repository.findAll(pageable).stream()
                    .map(UserMapper::mapperToUserDto)
                    .collect(Collectors.toList());
        } else {
            log.info("Выводим пользователей из списка ids: {}", ids);
            users = repository.findAllByIds(ids, pageable).stream()
                    .map(UserMapper::mapperToUserDto)
                    .collect(Collectors.toList());
        }
        return users;
    }

    @Override
    public UserDto add(NewUserRequestDto newUser) {
        log.info("Обрабатываем запрос на добавление нового пользователя {}", newUser);
        return UserMapper.mapperToUserDto(repository.save(UserMapper.mapperToUser(newUser)));
    }

    @Override
    public void delete(long userId) {
        log.info("Обрабатываем запрос на удаление пользователя {}", userId);
        repository.deleteById(userId);
    }
}
