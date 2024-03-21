package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.request.dto.ItemRequestFromRequest;
import ru.practicum.shareit.request.dto.ItemRequestResponse;
import ru.practicum.shareit.request.dto.ItemRequestWithOffersResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemRequestMapper {

    ItemRequest toItemRequest(ItemRequestFromRequest itemRequestFromRequest);

    ItemRequestResponse toItemRequestResponse(ItemRequest itemRequest);

    ItemRequestWithOffersResponse toItemRequestWithOffersResponse(ItemRequest itemRequest);
}
