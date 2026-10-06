package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDtoTest {

    private final JacksonTester<ItemDto> json;
    private final ItemDto dto = new ItemDto()
            .setId(1L)
            .setUserId(1L)
            .setName("Item")
            .setDescription("Item description")
            .setAvailable(true);

    @Test
    @DisplayName("Сериализация ItemDto")
    void testSerializeItemDto() throws Exception {
        JsonContent<ItemDto> result = json.write(dto);
        assertThat(result).extractingJsonPathValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.userId").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.name").isEqualTo("Item");
        assertThat(result).extractingJsonPathValue("$.description").isEqualTo("Item description");
        assertThat(result).extractingJsonPathValue("$.available").isEqualTo(true);
    }

    @Test
    @DisplayName("Десериализация ItemDto")
    void testDeserializeItemDto() throws Exception {
        String jsonString = "{" +
                            "\"id\": 1," +
                            "\"userId\": 1," +
                            "\"name\": \"Item\"," +
                            "\"description\": \"Item description\"," +
                            "\"available\": true" +
                            "}";
        ItemDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}
