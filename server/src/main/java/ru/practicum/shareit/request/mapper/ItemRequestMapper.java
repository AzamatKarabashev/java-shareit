package ru.practicum.shareit.request.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface ItemRequestMapper {

    ItemRequestMapper INSTANCE = Mappers.getMapper(ItemRequestMapper.class);

    ItemRequest toItemRequest(ItemRequestDto dto);

    @AfterMapping
    default void setCreatedTime(@MappingTarget ItemRequest target) {
        target.setCreated(LocalDateTime.now());
    }

    @Mappings({
            @Mapping(target = "requestorId", source = "source.requestor.id"),
            @Mapping(target = "items", ignore = true)
    })
    ItemRequestDto toItemRequestDto(ItemRequest source);
}
