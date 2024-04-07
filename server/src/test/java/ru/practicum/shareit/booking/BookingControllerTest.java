package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDtoWithItemAndUser;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.Constant.USER_HEADER;
import static ru.practicum.shareit.booking.model.BookingStatus.APPROVED;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService bookingService;
    @Autowired
    MockMvc mvc;

    @Test
    void testCreateBooking() throws Exception {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(1L);
        requestDto.setStart(LocalDateTime.now().plusDays(2));
        requestDto.setEnd(LocalDateTime.now().plusDays(3));

        BookingDtoWithItemAndUser responseDto = new BookingDtoWithItemAndUser();
        responseDto.setId(1L);
        when(bookingService.addRequest(any(BookingRequestDto.class), anyLong())).thenReturn(responseDto);
        mvc.perform(post("/bookings")
                        .header(USER_HEADER, "1")
                        .content(mapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class));
    }

    @Test
    void testUpdateBooking() throws Exception {
        BookingDtoWithItemAndUser responseDto = new BookingDtoWithItemAndUser();
        responseDto.setId(1L);
        responseDto.setStatus(APPROVED);
        when(bookingService.update(anyLong(), anyLong(), any(Boolean.class))).thenReturn(responseDto);
        mvc.perform(patch("/bookings/1")
                        .param("approved", "true")
                        .header(USER_HEADER, "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void testFindById() throws Exception {
        BookingDtoWithItemAndUser bookingDto = new BookingDtoWithItemAndUser();
        bookingDto.setId(1L);
        when(bookingService.findById(anyLong(), anyLong())).thenReturn(bookingDto);
        mvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class));
    }

    @Test
    void testFindBookersBookings() throws Exception {
        BookingDtoWithItemAndUser bookingDto = new BookingDtoWithItemAndUser();
        bookingDto.setId(1L);
        when(bookingService.findAllByUserAndState(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));
        mvc.perform(get("/bookings")
                        .header(USER_HEADER, 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1L), Long.class));
    }

    @Test
    void testFindOwnersBookings() throws Exception {
        BookingDtoWithItemAndUser bookingDto = new BookingDtoWithItemAndUser();
        bookingDto.setId(1L);
        when(bookingService.findAllByOwnerAndState(anyLong(), anyString(), anyInt(), anyInt())).thenReturn(List.of(bookingDto));
        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class));
    }
}
