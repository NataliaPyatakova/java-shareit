package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.NewCommentDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NewCommentDtoTest {

    private final JacksonTester<NewCommentDto> json;
    private final NewCommentDto dto = new NewCommentDto().setText("test text");

    @Test
    @DisplayName("Сериализация NewCommentDto")
    void testSerializeNewCommentDto() throws Exception {
        JsonContent<NewCommentDto> result = json.write(dto);
        assertThat(result).extractingJsonPathValue("$.text").isEqualTo("test text");
    }

    @Test
    @DisplayName("Десериализация NewCommentDto")
    void testDeserializeNewCommentDto() throws Exception {
        String jsonString = "{\"text\": \"test text\" }";
        NewCommentDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}
