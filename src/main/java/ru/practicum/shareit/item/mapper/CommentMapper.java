package ru.practicum.shareit.item.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static CommentDto commentToCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setCreated(comment.getDateCreated());
        commentDto.setAuthorName(comment.getUser().getName());
        return commentDto;
    }

    public static Comment commentDtoToCommentForCreate(User user, Item item, NewCommentDto newCommentDto) {
        Comment comment = new Comment();
        comment.setItem(item);
        comment.setUser(user);
        comment.setText(newCommentDto.getText());
        comment.setDateCreated(LocalDateTime.now());
        return comment;
    }
}