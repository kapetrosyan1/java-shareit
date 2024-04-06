package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.Sort.Direction.ASC;

@DataJpaTest
public class ItemRequestStorageTest {
    @Autowired
    private ItemRequestStorage itemRequestStorage;
    @Autowired
    private UserStorage userStorage;
    private final Sort sortIdAsc = Sort.by(ASC, "id");
    private final PageRequest pageRequest = PageRequest.of(0, 20, sortIdAsc);

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByAuthorId() {
        assertEquals(0, itemRequestStorage.findAllByAuthorId(1L, sortIdAsc).size());

        User user = new User();
        user.setName("name");
        user.setEmail("email@user.com");

        user = userStorage.save(user);

        User anotherUser = new User();
        anotherUser.setName("anotherUser");
        anotherUser.setEmail("anotherUser@user.com");

        anotherUser = userStorage.save(anotherUser);

        assertEquals(0, itemRequestStorage.findAllByAuthorId(user.getId(), sortIdAsc).size());

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setAuthor(user);
        itemRequest.setDescription("request");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequestStorage.save(itemRequest);

        assertEquals(1, itemRequestStorage.findAllByAuthorId(user.getId(), sortIdAsc).size());
        assertEquals(0, itemRequestStorage.findAllByAuthorId(anotherUser.getId(), sortIdAsc).size());

        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest2.setAuthor(user);
        itemRequest2.setDescription("new request");
        itemRequest2.setCreated(LocalDateTime.now());
        itemRequestStorage.save(itemRequest2);

        assertEquals(2, itemRequestStorage.findAllByAuthorId(user.getId(), sortIdAsc).size());
        assertEquals(0, itemRequestStorage.findAllByAuthorId(anotherUser.getId(), sortIdAsc).size());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByAuthorIdNot() {
        assertEquals(0, itemRequestStorage.findAllByAuthorIdNot(1L, pageRequest).getContent().size());

        User user = new User();
        user.setName("name");
        user.setEmail("email@user.com");

        user = userStorage.save(user);

        User anotherUser = new User();
        anotherUser.setName("anotherUser");
        anotherUser.setEmail("anotherUser@user.com");

        anotherUser = userStorage.save(anotherUser);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setAuthor(user);
        itemRequest.setDescription("request");
        itemRequest.setCreated(LocalDateTime.now());
        itemRequestStorage.save(itemRequest);

        assertEquals(0, itemRequestStorage.findAllByAuthorIdNot(user.getId(), pageRequest).getContent().size());
        assertEquals(1, itemRequestStorage.findAllByAuthorIdNot(anotherUser.getId(), pageRequest).getContent().size());

        ItemRequest itemRequest2 = new ItemRequest();
        itemRequest2.setAuthor(user);
        itemRequest2.setDescription("new request");
        itemRequest2.setCreated(LocalDateTime.now());
        itemRequestStorage.save(itemRequest2);

        assertEquals(0, itemRequestStorage.findAllByAuthorIdNot(user.getId(), pageRequest).getContent().size());
        assertEquals(2, itemRequestStorage.findAllByAuthorIdNot(anotherUser.getId(), pageRequest).getContent().size());
    }
}
