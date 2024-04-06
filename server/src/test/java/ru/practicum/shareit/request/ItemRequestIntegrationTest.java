package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.service.impl.ItemServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.impl.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ItemRequestIntegrationTest {
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private ItemServiceImpl itemService;
    @Autowired
    private ItemRequestServiceImpl requestService;
    private User requester;
    private User owner;

    @BeforeEach
    void setup() {
        requester = new User();
        requester.setName("requester");
        requester.setEmail("requester@user.com");

        owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@user.com");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testCreateItemRequest() {
        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("need item");

        assertThrows(NotFoundException.class, () -> requestService.create(requestDto, 1L),
                "Пользователей пока не зарегистрировано");

        requester = userStorage.save(requester);
        ItemRequestResponseDto responseDto = requestService.create(requestDto, requester.getId());

        assertEquals(responseDto.getDescription(), requestService.findById(requester.getId(), responseDto.getId()).getDescription());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByAuthorId() {
        assertThrows(NotFoundException.class, () -> requestService.findAllByAuthorId(1L));

        requester = userStorage.save(requester);
        owner = userStorage.save(owner);

        assertEquals(0, requestService.findAllByAuthorId(requester.getId()).size());

        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("need item1");

        ItemRequestResponseDto request = requestService.create(requestDto, requester.getId());

        requestDto.setDescription("need item2");

        requestService.create(requestDto, requester.getId());

        requestDto.setDescription("owner request");

        assertEquals(2, requestService.findAllByAuthorId(requester.getId()).size());
        assertEquals(0, requestService.findAllByAuthorId(owner.getId()).size());

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("item");
        itemRequestDto.setDescription("desc");
        itemRequestDto.setAvailable(true);
        itemRequestDto.setRequestId(request.getId());

        itemService.create(requester.getId(), itemRequestDto);

        assertEquals(0, requestService.findAllByAuthorId(requester.getId()).get(0).getItems().size());
        assertEquals(itemRequestDto.getName(),
                requestService.findAllByAuthorId(requester.getId()).get(1).getItems().get(0).getName());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllRequests() {
        assertThrows(NotFoundException.class, () -> requestService.findAllRequests(1L, 0, 20));

        requester = userStorage.save(requester);
        owner = userStorage.save(owner);

        assertEquals(0, requestService.findAllRequests(requester.getId(), 0, 20).size());

        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("need item1");
        ItemRequestResponseDto request = requestService.create(requestDto, requester.getId());
        requestDto.setDescription("need item2");
        requestService.create(requestDto, requester.getId());
        requestDto.setDescription("owner request");

        assertEquals(0, requestService.findAllRequests(owner.getId(), 0, 20).get(0).getItems().size());
        assertEquals(2, requestService.findAllRequests(owner.getId(), 0, 20).size());
        assertEquals(0, requestService.findAllRequests(requester.getId(), 0, 20).size());

        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("item");
        itemRequestDto.setDescription("desc");
        itemRequestDto.setAvailable(true);
        itemRequestDto.setRequestId(request.getId());
        itemService.create(owner.getId(), itemRequestDto);

        assertEquals(1, requestService.findAllRequests(owner.getId(), 0, 20)
                .get(1).getItems().size());
        assertEquals(0, requestService.findAllRequests(owner.getId(), 0, 20)
                .get(0).getItems().size());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindById() {
        assertThrows(NotFoundException.class, () -> requestService.findById(1L, 1L));

        requester = userStorage.save(requester);

        assertThrows(NotFoundException.class, () -> requestService.findById(1L, 1L));

        ItemRequestRequestDto requestDto = new ItemRequestRequestDto();
        requestDto.setDescription("need item1");
        ItemRequestResponseDto request = requestService.create(requestDto, requester.getId());

        assertEquals(request.getDescription(), requestService.findById(requester.getId(), request.getId()).getDescription());
        assertEquals(0, requestService.findById(requester.getId(), request.getId()).getItems().size());

        owner = userStorage.save(owner);
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("item");
        itemRequestDto.setDescription("desc");
        itemRequestDto.setAvailable(true);
        itemRequestDto.setRequestId(request.getId());
        itemService.create(owner.getId(), itemRequestDto);

        assertEquals(1, requestService.findById(requester.getId(), request.getId()).getItems().size());
    }
}
