package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestDtoTest {

    private final JacksonTester<ItemRequestDto> json;
    private final ItemRequestDto dto = new ItemRequestDto()
            .setId(1L)
            .setDescription("test description")
            .setCreated(LocalDateTime.parse("2026-10-06T12:27:36"));

    @Test
    @DisplayName("Сериализация ItemRequestDto")
    void testSerializeItemRequestDto() throws Exception {
        JsonContent<ItemRequestDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathValue("$.created").isEqualTo("2026-10-06T12:27:36");
    }

    @Test
    @DisplayName("Десериализация ItemRequestDto")
    void testDeserializeItemRequestDto() throws Exception {
        String jsonString = "{" +
                            "\"id\" : 1," +
                            "\"description\": \"test description\"," +
                            "\"created\" : \"2026-10-06T12:27:36\"" +
                            "}";
        ItemRequestDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}