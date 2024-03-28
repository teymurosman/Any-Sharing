package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // Без NoArgs и AllArgs тесты по comments падают (JSON parse error: Cannot construct instance)
@Builder
public class CommentFromRequest {

    private String text;
}
