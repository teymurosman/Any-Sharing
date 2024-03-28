package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.item.dto.CommentFromRequest;
import ru.practicum.shareit.item.dto.CommentResponse;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = LocalDateTime.class)
public interface CommentMapper {

    @Mapping(target = "created", expression = "java(LocalDateTime.now())")
    Comment toComment(CommentFromRequest commentFromRequest);

    @Mapping(target = "authorName", source = "comment.author.name")
    CommentResponse toCommentResponse(Comment comment);

    Set<CommentResponse> toSetCommentResponse(Set<Comment> comments);
}
