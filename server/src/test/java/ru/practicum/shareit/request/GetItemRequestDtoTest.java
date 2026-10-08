package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.GetItemRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GetItemRequestDtoTest {

    private final JacksonTester<GetItemRequestDto> json;
    private final List<ItemDto> items = new ArrayList<>();
    private final ItemDto item = new ItemDto()
            .setId(1L)
            .setUserId(1L)
            .setName("test item")
            .setDescription("test item description")
            .setAvailable(true);
    private final GetItemRequestDto dto = new GetItemRequestDto()
            .setId(1L)
            .setDescription("test description")
            .setCreated(LocalDateTime.parse("2026-10-06T12:27:36"))
            .setItems(items);

    @Test
    @DisplayName("Сериализация GetItemRequestDto")
    void testSerializeGetItemRequestDto() throws Exception {
        items.add(item);
        JsonContent<GetItemRequestDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathValue("$.created").isEqualTo("2026-10-06T12:27:36");
        assertThat(result).extractingJsonPathNumberValue("$.items.[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.items.[0].userId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items.[0].name").isEqualTo(item.getName());
        assertThat(result).extractingJsonPathStringValue("$.items.[0].description").isEqualTo(item.getDescription());
        assertThat(result).extractingJsonPathValue("$.items.[0].available").isEqualTo(item.getAvailable());
    }

    @Test
    @DisplayName("Десериализация GetItemRequestDto")
    void testDeserializeGetItemRequestDto() throws Exception {
        items.add(item);
        String jsonString = "{" +
                            "\"id\" : 1," +
                            "\"description\": \"test description\"," +
                            "\"created\" : \"2026-10-06T12:27:36\"," +
                            "\"items\": [{" +
                                        "\"id\": 1," +
                                        "\"userId\": 1," +
                                        "\"name\": \"test item\"," +
                                        "\"description\": \"test item description\"," +
                                        "\"available\": true}]" +
                            "}";
        GetItemRequestDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}
