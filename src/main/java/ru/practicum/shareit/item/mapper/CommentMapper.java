package ru.practicum.shareit.item.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true))
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(target = "created", ignore = true)
    Comment toComment(CommentDto source);

    @AfterMapping
    default void setCreatedTime(@MappingTarget Comment comment) {
        comment.setCreated(LocalDateTime.now());
    }

    @Mapping(target = "authorName", source = "author.name")
    CommentDto toCommentDto(Comment source);
}