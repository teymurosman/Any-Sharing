package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ItemCreateRequest {

    @NotBlank(message = "Название вещи не может быть пустым.")
    private String name;

    @NotBlank(message = "Описание вещи не может быть пустым.")
    private String description;

    @NotNull(message = "Статус вещи должен быть указан.")
    private Boolean available;
}
