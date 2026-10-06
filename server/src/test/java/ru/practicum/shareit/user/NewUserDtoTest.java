package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.NewUserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class NewUserDtoTest {

    private final JacksonTester<NewUserDto> json;

    @Test
    @DisplayName("Сериализация NewUserDto")
    void testSerializeUserDto() throws Exception {
        NewUserDto userDto = new NewUserDto().setEmail("test_email@email.ru").setName("test_name");
        JsonContent<NewUserDto> result = json.write(userDto);
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("test_email@email.ru");
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("test_name");
    }

    @Test
    @DisplayName("Десериализация NewUserDto")
    void testDeserializeUserDto() throws Exception {
        String jsonString = """
                {
                "email" : "test_email@email.ru",
                "name" : "test_name"
                }
                """;
        NewUserDto userDto = json.parseObject(jsonString);
        assertThat(userDto).isEqualTo(new NewUserDto().setEmail("test_email@email.ru").setName("test_name"));
    }
}