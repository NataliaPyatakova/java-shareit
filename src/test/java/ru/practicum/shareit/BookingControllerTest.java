package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.booking.enumeration.BookingProcessState;
import ru.practicum.shareit.booking.enumeration.BookingStateSearch;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    private final LocalDateTime startTime = LocalDateTime.now();
    private final LocalDateTime endTime = LocalDateTime.now();
    private final UserDto userDto = new UserDto()
            .setId(1L)
            .setName("New User")
            .setEmail("new_user@email.ru");
    private final ItemDto item = new ItemDto()
            .setId(1L)
            .setName("New Item")
            .setUserId(2L)
            .setAvailable(true)
            .setDescription("New Item");
    private final BookingDto bookingDto = new BookingDto()
            .setId(1L)
            .setBooker(userDto)
            .setItem(item)
            .setStart(startTime)
            .setEnd(endTime)
            .setStatus(BookingProcessState.WAITING);
    private final BookingDto updatedBookingDto = new BookingDto()
            .setId(1L)
            .setBooker(userDto)
            .setItem(item)
            .setStart(startTime)
            .setEnd(endTime)
            .setStatus(BookingProcessState.APPROVED);
    private final NewBookingDto newBookingDto = new NewBookingDto()
            .setItemId(1L)
            .setStart(startTime)
            .setEnd(endTime);
    private final List<BookingDto> listBookingDto = new ArrayList<>();

    @Test
    public void testSave() throws Exception {
        when(bookingService.save(newBookingDto, userDto.getId())).thenReturn(bookingDto);
        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(newBookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(item.getName())))
                .andExpect(jsonPath("$.booker.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(userDto.getName())))
                .andExpect(jsonPath("$.status", is(BookingProcessState.WAITING.toString())));
    }

    @Test
    public void testUpdate() throws Exception {
        when(bookingService.update(bookingDto.getId(), userDto.getId(), true)).thenReturn(updatedBookingDto);
        mockMvc.perform(patch("/bookings/" + bookingDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userDto.getId().toString())
                        .param("approved", String.valueOf(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(item.getName())))
                .andExpect(jsonPath("$.booker.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(userDto.getName())))
                .andExpect(jsonPath("$.status", is(BookingProcessState.APPROVED.toString())));
    }

    @Test
    public void testFindById() throws Exception {
        when(bookingService.findById(bookingDto.getId(), userDto.getId())).thenReturn(bookingDto);
        mockMvc.perform(get("/bookings/" + bookingDto.getId())
                        .header("X-Sharer-User-Id", userDto.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(item.getName())))
                .andExpect(jsonPath("$.booker.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.booker.name", is(userDto.getName())))
                .andExpect(jsonPath("$.status", is(BookingProcessState.WAITING.toString())));
    }

    @Test
    public void testFindByBookerIdAndState() throws Exception {
        listBookingDto.add(bookingDto);
        when(bookingService.findByBookerIdAndState(userDto.getId(), BookingStateSearch.ALL)).thenReturn(listBookingDto);
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userDto.getId().toString())
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(updatedBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.name", is(item.getName())))
                .andExpect(jsonPath("$.[0].booker.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.name", is(userDto.getName())))
                .andExpect(jsonPath("$.[0].status", is(BookingProcessState.WAITING.toString())));
    }

    @Test
    public void testFindByOwnerIdAndState() throws Exception {
        listBookingDto.add(bookingDto);
        when(bookingService.findByOwnerIdAndState(userDto.getId(), BookingStateSearch.ALL)).thenReturn(listBookingDto);
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userDto.getId().toString())
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(updatedBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.id", is(item.getId()), Long.class))
                .andExpect(jsonPath("$.[0].item.name", is(item.getName())))
                .andExpect(jsonPath("$.[0].booker.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].booker.name", is(userDto.getName())))
                .andExpect(jsonPath("$.[0].status", is(BookingProcessState.WAITING.toString())));
    }
}
