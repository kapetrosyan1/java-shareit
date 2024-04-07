package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.CommentRequestDto;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDtoWithBookings;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

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

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemService itemService;

    private static User user;
    private static ItemResponseDto responseDto;
    private static ItemResponseDtoWithBookings responseDtoWithBookings;

    @BeforeAll
    public static void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("user");
        user.setEmail("user@user.com");

        responseDto = new ItemResponseDto();
        responseDto.setId(1L);
        responseDto.setName("item");
        responseDto.setDescription("item description");
        responseDto.setAvailable(true);

        responseDtoWithBookings = new ItemResponseDtoWithBookings();
        responseDtoWithBookings.setId(1L);
        responseDtoWithBookings.setName("item");
        responseDtoWithBookings.setDescription("item description");
        responseDtoWithBookings.setAvailable(true);
    }

    @Test
    void testSaveItem() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("item");
        itemRequestDto.setDescription("item description");
        itemRequestDto.setAvailable(true);

        when(itemService.create(anyLong(), any(ItemRequestDto.class))).thenReturn(responseDto);
        mvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(responseDto.getName())))
                .andExpect(jsonPath("$.description", is(responseDto.getDescription())))
                .andExpect(jsonPath("$.available", is(responseDto.getAvailable())));
    }

    @Test
    void testUpdateItem() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any(ItemRequestDto.class))).thenReturn(responseDto);
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header(USER_HEADER, "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testFindById() throws Exception {
        when(itemService.findById(anyLong(), anyLong())).thenReturn(responseDtoWithBookings);

        mvc.perform(get("/items/1")
                        .header(USER_HEADER, "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDtoWithBookings.getId()), Long.class));
    }

    @Test
    void testDeleteItem() throws Exception {
        mvc.perform(delete("/items/2")
                        .header(USER_HEADER, "1"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindOwnersItems() throws Exception {
        when(itemService.findOwnersItems(anyLong(), anyInt(), anyInt())).thenReturn(List.of(responseDtoWithBookings));

        mvc.perform(get("/items")
                        .header(USER_HEADER, "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(responseDtoWithBookings.getId()), Long.class));
    }

    @Test
    void testSearchItems() throws Exception {
        when(itemService.searchItems(anyString(), anyInt(), anyInt())).thenReturn(List.of(responseDto));

        mvc.perform(get("/items/search")
                        .header(USER_HEADER, "1")
                        .param("text", "desc")
                        .header(USER_HEADER, "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(responseDto.getId()), Long.class));
    }

    @Test
    void testAddComment() throws Exception {
        CommentRequestDto commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("comment");

        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(1L);
        commentResponseDto.setText("comment");
        commentResponseDto.setAuthorName(user.getName());
        commentResponseDto.setCreated(LocalDateTime.now());
        when(itemService.addComment(anyLong(), anyLong(), any(CommentRequestDto.class))).thenReturn(commentResponseDto);

        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentRequestDto))
                        .header("X-Sharer-User-Id", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentResponseDto.getId()), Long.class));
    }
}
