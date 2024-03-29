package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class ItemStorageTest {
    @Autowired
    private ItemStorage itemStorage;
    @Autowired
    private UserStorage userStorage;
    @Autowired
    private ItemRequestStorage itemRequestStorage;

    private User user;
    private Item item1;
    private Item item2;
    public static ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("user");
        user.setEmail("user@user.com");
        user = userStorage.save(user);

        User user1 = new User();
        user1.setName("user1");
        user1.setEmail("user1@user.com");
        user1 = userStorage.save(user1);

        itemRequest = new ItemRequest();
        itemRequest.setAuthor(user1);
        itemRequest.setDescription("item");
        itemRequest = itemRequestStorage.save(itemRequest);

        item1 = new Item();
        item1.setName("item1");
        item1.setDescription("item1Desc");
        item1.setOwner(user);
        item1.setAvailable(true);
        item1.setItemRequest(itemRequest);

        item2 = new Item();
        item2.setAvailable(true);
        item2.setName("item2");
        item2.setDescription("item2Desc");
        item2.setOwner(user);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllByOwner() {
        itemStorage.save(item1);
        itemStorage.save(item2);
        List<Item> items = itemStorage.findAllByOwnerId(1L, PageRequest.of(0, 10,
                Sort.by(Sort.Direction.ASC, "id"))).getContent();
        assertEquals(2, items.size());
        assertEquals(1L, items.get(0).getId());
        assertEquals(2L, items.get(1).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindAllOwnersItemsIds() {
        itemStorage.save(item1);
        itemStorage.save(item2);
        List<Long> ids = itemStorage.findAllOwnersItemsIds(1L);
        assertEquals(2, ids.size());
        assertEquals(1L, ids.get(0));
        assertEquals(2L, ids.get(1));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testSearchItems() {
        itemStorage.save(item1);
        itemStorage.save(item2);
        List<Item> items = itemStorage.searchItems("em1", PageRequest.of(0, 10)).getContent();
        assertEquals(1, items.size());
        assertEquals(1L, items.get(0).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindByItemRequestIn() {
        item1.setItemRequest(itemRequest);
        itemStorage.save(item1);
        itemStorage.save(item2);
        List<Item> items = itemStorage.findByItemRequestIn(List.of(itemRequest), Sort.by(Sort.Direction.ASC, "id"));
        assertEquals(1, items.size());
        assertEquals(1L, items.get(0).getId());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testFindByItemRequestId() {
        itemStorage.save(item1);
        itemStorage.save(item2);
        List<Item> items = itemStorage.findByItemRequestId(1L, Sort.by(Sort.Direction.ASC, "id"));
        assertEquals(1, items.size());
        assertEquals(1L, items.get(0).getId());
    }
}
