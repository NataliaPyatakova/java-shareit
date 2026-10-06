package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NewBookingDtoTest {

    private final JacksonTester<NewBookingDto> json;
    private final NewBookingDto dto = new NewBookingDto()
            .setItemId(1L)
            .setStart(LocalDateTime.parse("2026-10-06T12:27:36"))
            .setEnd(LocalDateTime.parse("2026-10-07T12:27:36"));

    @Test
    @DisplayName("Сериализация NewBookingDto")
    void testSerializeNewBookingDto() throws Exception {
        JsonContent<NewBookingDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathValue("$.start").isEqualTo("2026-10-06T12:27:36");
        assertThat(result).extractingJsonPathValue("$.end").isEqualTo("2026-10-07T12:27:36");
    }

    @Test
    @DisplayName("Десериализация NewBookingDto")
    void testDeserializeNewBookingDto() throws Exception {
        String jsonString = """
                {
                    "itemId": 1,
                    "start": "2026-10-06T12:27:36",
                    "end": "2026-10-07T12:27:36"
                }
                """;
        NewBookingDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}
