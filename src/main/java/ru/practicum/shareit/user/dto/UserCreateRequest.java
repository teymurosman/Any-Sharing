package ru.practicum.shareit.user.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UserCreateRequest {

    @NotBlank(message = "Имя пользователя не может быть пустым.")
    private String name;

    @NotBlank(message = "Почта не может быть пустой.")
    @Email(regexp = ".+[@].+[\\.].+", message = "Неверный формат почты.")
    private String email;
}
