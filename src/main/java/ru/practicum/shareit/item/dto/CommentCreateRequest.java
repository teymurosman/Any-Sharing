package ru.practicum.shareit.item.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CommentCreateRequest {

    @NotBlank(message = "Комментарий должен содержать текст.")
    private String text;


}
