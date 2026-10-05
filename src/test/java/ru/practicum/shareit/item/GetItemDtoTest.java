package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.GetItemDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetItemDtoTest {

    private final JacksonTester<GetItemDto> json;
    private final CommentDto comment = new CommentDto()
            .setId(1L)
            .setText("This is a comment")
            .setAuthorName("John")
            .setCreated(LocalDateTime.parse("2026-10-06T12:27:36"));
    private final List<CommentDto> comments = new ArrayList<>();
    private final GetItemDto dto = new GetItemDto()
            .setId(1L)
            .setUserId(1L)
            .setName("Item")
            .setDescription("Item description")
            .setAvailable(true)
            .setLastBooking(LocalDateTime.parse("2026-10-06T12:27:36"))
            .setNextBooking(LocalDateTime.parse("2026-10-07T12:27:36"))
            .setComments(comments);

    @Test
    @DisplayName("Сериализация GetItemDto")
    void testSerializeGetItemDto() throws Exception {
        comments.add(comment);
        JsonContent<GetItemDto> result = json.write(dto);
        assertThat(result).extractingJsonPathValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.userId").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.name").isEqualTo("Item");
        assertThat(result).extractingJsonPathValue("$.description").isEqualTo("Item description");
        assertThat(result).extractingJsonPathValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathValue("$.comments.[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.comments.[0].text").isEqualTo("This is a comment");
        assertThat(result).extractingJsonPathValue("$.comments.[0].authorName").isEqualTo("John");
        assertThat(result).extractingJsonPathValue("$.comments.[0].created").isEqualTo("2026-10-06T12:27:36");
        assertThat(result).extractingJsonPathValue("$.lastBooking").isEqualTo("2026-10-06T12:27:36");
        assertThat(result).extractingJsonPathValue("$.nextBooking").isEqualTo("2026-10-07T12:27:36");
    }

    @Test
    @DisplayName("Десериализация GetItemDto")
    void testDeserializeGetItemDto() throws Exception {
        comments.add(comment);
        String jsonString = """
                {
                    "id": 1,
                    "userId": 1,
                    "name": "Item",
                    "description": "Item description",
                    "available": true,
                    "comments": [
                        {
                            "id": 1,
                            "text": "This is a comment",
                            "authorName": "John",
                            "created": "2026-10-06T12:27:36"
                        }
                    ],
                    "lastBooking": "2026-10-06T12:27:36",
                    "nextBooking": "2026-10-07T12:27:36"
                }
                """;
        GetItemDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}
