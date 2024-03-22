package ru.practicum.shareit.common;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public class PageableFactory {
    public static Pageable getPageable(int from, int size) {
        return PageRequest.of(from / size, size);
    }

    public static Pageable getPageable(int from, int size, Sort sort) {
        return PageRequest.of(from / size, size, sort);
    }
}
