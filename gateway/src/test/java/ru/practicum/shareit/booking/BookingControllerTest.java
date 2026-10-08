package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.NewBookingDto;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    private final LocalDateTime startTime = LocalDateTime.now();
    private final LocalDateTime endTime = LocalDateTime.now();

    private final NewBookingDto newBookingDto = new NewBookingDto()
            .setItemId(1L)
            .setStart(startTime)
            .setEnd(endTime);

    @Test
    @DisplayName("Post bookings - без вещи")
    public void testSaveWithNoItemId() throws Exception {
        newBookingDto.setItemId(null);
        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(newBookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Post bookings - без начала")
    public void testSaveWithNoStart() throws Exception {
        newBookingDto.setStart(null);
        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(newBookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Post bookings - без конца")
    public void testSaveWithNoEnd() throws Exception {
        newBookingDto.setEnd(null);
        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(newBookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Patch bookings - без id")
    public void testUpdateWithNoId() throws Exception {
        mockMvc.perform(patch("/bookings/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", String.valueOf(true)))
                .andExpect(status().isNotFound());
    }
}