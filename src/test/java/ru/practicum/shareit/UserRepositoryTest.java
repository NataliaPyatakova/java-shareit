package ru.practicum.shareit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private final User user = new User().setName("New user").setEmail("new_email@email.ru");

    @Test
    public void testSave() {
        User result = userRepository.save(user);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getName(), result.getName());
        Assertions.assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    public void testFindByEmail() {
        userRepository.save(user);
        Optional<User> optUser = userRepository.findByEmail(user.getEmail());
        Assertions.assertNotNull(optUser);
    }
}
