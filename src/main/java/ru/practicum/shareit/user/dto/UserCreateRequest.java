package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserCreateRequest {

    @NotBlank(message = "Имя пользователя не может быть пустым.")
    private String name;

    @NotBlank(message = "Почта не может быть пустой.")
    @Email(regexp = ".+[@].+[\\.].+", message = "Неверный формат почты.")
    private String email;
}
