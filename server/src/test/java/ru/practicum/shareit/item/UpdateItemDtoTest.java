package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UpdateItemDtoTest {

    private final JacksonTester<UpdateItemDto> json;
    private final UpdateItemDto dto = new UpdateItemDto()
            .setName("test item")
            .setDescription("test description")
            .setAvailable(true);

    @Test
    @DisplayName("Сериализация UpdateItemDto")
    void testSerializeUpdateItemDto() throws Exception {
        JsonContent<UpdateItemDto> result = json.write(dto);
        assertThat(result).extractingJsonPathValue("$.name").isEqualTo("test item");
        assertThat(result).extractingJsonPathValue("$.description").isEqualTo("test description");
        assertThat(result).extractingJsonPathValue("$.available").isEqualTo(true);
    }

    @Test
    @DisplayName("Десериализация UpdateItemDto")
    void testDeserializeUpdateItemDto() throws Exception {
        String jsonString = "{" +
                            "\"name\": \"test item\"," +
                            "\"description\": \"test description\"," +
                            "\"available\": true" +
                            "}";
        UpdateItemDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}
