package ru.practicum.shareit.user.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    private String name;

    private String email;
}
