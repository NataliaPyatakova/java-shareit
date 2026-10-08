package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.NewCommentDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@SpringBootTest
public class CommentMapperTest {

    private final LocalDateTime date = LocalDateTime.now();
    private final User user = new User()
            .setId(1L)
            .setName("user")
            .setEmail("email@email.ru");
    private final Item item = new Item()
            .setId(1L)
            .setName("item")
            .setDescription("test")
            .setUser(user);
    private final User commentator = new User()
            .setId(2L)
            .setName("commentator")
            .setEmail("commentator@email.ru");
    private final Comment comment = new Comment()
            .setId(1L)
            .setUser(commentator)
            .setItem(item)
            .setText("test")
            .setDateCreated(date);
    private final NewCommentDto newCommentDto = new NewCommentDto().setText("test");

    @Test
    public void testCommentToCommentDto() {
        CommentDto result = CommentMapper.commentToCommentDto(comment);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(comment.getId(), result.getId());
        Assertions.assertEquals(comment.getText(), result.getText());
        Assertions.assertEquals(comment.getDateCreated(), result.getCreated());
        Assertions.assertEquals(comment.getUser().getName(), result.getAuthorName());
    }

    @Test
    public void testCommentDtoToCommentForCreate() {
        Comment result = CommentMapper.commentDtoToCommentForCreate(commentator, item, newCommentDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newCommentDto.getText(), result.getText());
        Assertions.assertEquals(item, result.getItem());
        Assertions.assertEquals(commentator, result.getUser());
    }
}