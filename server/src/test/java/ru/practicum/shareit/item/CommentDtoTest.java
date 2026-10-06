package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentDtoTest {

    private final JacksonTester<CommentDto> json;
    private final CommentDto dto = new CommentDto()
            .setId(1L)
            .setText("test text")
            .setAuthorName("test_author")
            .setCreated(LocalDateTime.parse("2026-10-06T12:27:36"));

    @Test
    @DisplayName("Сериализация CommentDto")
    void testSerializeCommentDto() throws Exception {
        JsonContent<CommentDto> result = json.write(dto);
        assertThat(result).extractingJsonPathValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.text").isEqualTo("test text");
        assertThat(result).extractingJsonPathValue("$.authorName").isEqualTo("test_author");
        assertThat(result).extractingJsonPathValue("$.created").isEqualTo("2026-10-06T12:27:36");
    }

    @Test
    @DisplayName("Десериализация CommentDto")
    void testDeserializeCommentDto() throws Exception {
        String jsonString = """
                {
                    "id": 1,
                    "text": "test text",
                    "authorName": "test_author",
                    "created": "2026-10-06T12:27:36"
                }
                """;
        CommentDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}
