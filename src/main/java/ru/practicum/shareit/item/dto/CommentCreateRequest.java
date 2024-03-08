package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor // Без NoArgs и AllArgs тесты по comments падают (JSON parse error: Cannot construct instance)
@Builder
public class CommentCreateRequest {

    @NotBlank(message = "Комментарий должен содержать текст.")
    private String text;
}
