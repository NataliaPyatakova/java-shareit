package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@SpringBootTest
public class BookingMapperTest {

    private final LocalDateTime date = LocalDateTime.now();
    private final LocalDateTime date1 = LocalDateTime.MAX;
    private final User user = new User()
            .setId(1L)
            .setName("user")
            .setEmail("email@email.ru");
    private final Item item = new Item()
            .setId(1L)
            .setName("item")
            .setDescription("test")
            .setAvailable(true)
            .setUser(user);
    private final User booker = new User()
            .setId(2L)
            .setName("booker")
            .setEmail("booker@email.ru");
    private final Booking booking = new Booking()
            .setId(1L)
            .setItem(item)
            .setBooker(booker)
            .setStart(date)
            .setEnd(date1)
            .setState(BookingProcessState.WAITING);
    private final NewBookingDto newBookingDto = new NewBookingDto()
            .setItemId(item.getId())
            .setStart(date)
            .setEnd(date1);

    @Test
    public void testMapToBookingDtoTest() {
        BookingDto result = BookingMapper.mapToBookingDto(booking);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(booking.getId(), result.getId());
        Assertions.assertEquals(booking.getItem().getId(), result.getItem().getId());
        Assertions.assertEquals(booking.getItem().getName(), result.getItem().getName());
        Assertions.assertEquals(booking.getItem().getDescription(), result.getItem().getDescription());
        Assertions.assertEquals(booking.getItem().getAvailable(), result.getItem().getAvailable());
        Assertions.assertEquals(booking.getBooker().getId(), result.getBooker().getId());
        Assertions.assertEquals(booking.getBooker().getName(), result.getBooker().getName());
        Assertions.assertEquals(booking.getStart(), result.getStart());
        Assertions.assertEquals(booking.getEnd(), result.getEnd());
    }

    @Test
    public void testMapToBookingForCreate() {
        Booking result = BookingMapper.mapToBookingForCreate(newBookingDto, booker, item);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(item, result.getItem());
        Assertions.assertEquals(booker, result.getBooker());
        Assertions.assertEquals(newBookingDto.getStart(), result.getStart());
        Assertions.assertEquals(newBookingDto.getEnd(), result.getEnd());
        Assertions.assertEquals(BookingProcessState.WAITING, result.getState());
    }
}
