package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDtoTest {

    private final JacksonTester<BookingDto> json;
    private final UserDto booker = new UserDto()
            .setId(1L)
            .setEmail("test_email@email.ru")
            .setName("test_name");
    private final ItemDto item = new ItemDto()
            .setId(1L)
            .setUserId(1L)
            .setName("test item")
            .setDescription("test item description")
            .setAvailable(true);
    private final BookingDto dto = new BookingDto()
            .setId(1L)
            .setBooker(booker)
            .setItem(item)
            .setStart(LocalDateTime.parse("2026-10-06T12:27:36"))
            .setEnd(LocalDateTime.parse("2026-10-07T12:27:36"))
            .setStatus(BookingProcessState.WAITING);

    @Test
    @DisplayName("Сериализация BookingDto")
    void testSerializeBookingDto() throws Exception {
        JsonContent<BookingDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.item.userId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo(item.getName());
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo(item.getDescription());
        assertThat(result).extractingJsonPathValue("$.item.available").isEqualTo(item.getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo(booker.getEmail());
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo(booker.getName());
        assertThat(result).extractingJsonPathValue("$.start").isEqualTo("2026-10-06T12:27:36");
        assertThat(result).extractingJsonPathValue("$.end").isEqualTo("2026-10-07T12:27:36");
        assertThat(result).extractingJsonPathValue("$.status").isEqualTo(BookingProcessState.WAITING.toString());
    }

    @Test
    @DisplayName("Десериализация BookingDto")
    void testDeserializeBookingDto() throws Exception {
        String jsonString = """
                {    "id": 1,
                     "item": {
                         "id": 1,
                         "userId": 1,
                         "name": "test item",
                         "description": "test item description",
                         "available": true
                     },
                     "booker": {
                         "id": 1,
                         "email": "test_email@email.ru",
                         "name": "test_name"
                     },
                     "start": "2026-10-06T12:27:36",
                     "end": "2026-10-07T12:27:36",
                     "status": "WAITING"
                }
                """;
        BookingDto result = json.parseObject(jsonString);
        assertThat(result).isEqualTo(dto);
    }
}
