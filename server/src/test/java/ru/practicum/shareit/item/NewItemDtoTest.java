package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.NewItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NewItemDtoTest {

    private final JacksonTester<NewItemDto> json;
    private final NewItemDto dto = new NewItemDto()
            .setName("test name")
            .setDescription("test description")
            .setAvailable(true)
            .setRequestId(1L);

    @Test
    @DisplayName("Сериализация NewItemDto")
    void testSerializeNewItemDto() throws Exception {
        JsonContent<NewItemDto> result = json.write(dto);
        assertThat(result).extractingJsonPathValue("$.name").isEqualTo("test name");
        assertThat(result).extractingJsonPathValue("$.description").isEqualTo("test description");
        assertThat(result).extractingJsonPathValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathValue("$.requestId").isEqualTo(1);
    }

    @Test
    @DisplayName("Десериализация NewItemDto")
    void testDeserializeNewItemDto() throws Exception {
        String jsonString = "{" +
                            "\"name\": \"test name\"," +
                            "\"description\": \"test description\"," +
                            "\"available\": true," +
                            "\"requestId\": 1" +
                            "}";
        NewItemDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}
