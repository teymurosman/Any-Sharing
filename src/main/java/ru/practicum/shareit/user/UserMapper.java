package ru.practicum.shareit.user;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.user.dto.UserCreateRequest;
import ru.practicum.shareit.user.dto.UserResponse;
import ru.practicum.shareit.user.dto.UserUpdateRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User toUser(UserCreateRequest userCreateRequest);

    User toUser(UserUpdateRequest userUpdateRequest);

    UserResponse toUserResponse(User user);
}
