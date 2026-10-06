package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDtoTest {

    private final JacksonTester<UserDto> json;

    @Test
    @DisplayName("Сериализация UserDto")
    void testSerializeUserDto() throws Exception {
        UserDto userDto = new UserDto().setId(1L).setEmail("test_email@email.ru").setName("test_name");
        JsonContent<UserDto> result = json.write(userDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("test_email@email.ru");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("test_name");
    }

    @Test
    @DisplayName("Десериализация UserDto")
    void testDeserializeUserDto() throws Exception {
        String jsonString = """
                {
                "id": 1,
                "email" : "test_email@email.ru",
                "name" : "test_name"
                }
                """;
        UserDto userDto = json.parseObject(jsonString);
        assertThat(userDto).isEqualTo(new UserDto().setId(1L).setEmail("test_email@email.ru").setName("test_name"));
    }
}
