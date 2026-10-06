package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.NewItemRequestDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NewItemRequestDtoTest {

    private final JacksonTester<NewItemRequestDto> json;
    private final NewItemRequestDto dto = new NewItemRequestDto().setDescription("test description");

    @Test
    @DisplayName("Сериализация NewItemRequestDto")
    void testSerializeNewItemRequestDto() throws Exception {
        JsonContent<NewItemRequestDto> result = json.write(dto);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
    }

    @Test
    @DisplayName("Десериализация NewItemRequestDto")
    void testDeserializeNewItemRequestDto() throws Exception {
        String jsonString = """
                {
                     "description": "test description"
                }
                """;
        NewItemRequestDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}