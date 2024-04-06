package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> addItem(long userId, ItemRequestDto itemRequestDto) {
        return post("", userId, itemRequestDto);
    }

    public ResponseEntity<Object> commentItem(long userId, Long itemId, CommentRequestDto commentRequestDto) {
        return post(String.format("/%d/comment", itemId), userId, commentRequestDto);
    }

    public ResponseEntity<Object> updateItem(long userId, Long itemId, ItemRequestDto itemRequestDto) {
        return patch(String.format("/%d", itemId), userId, itemRequestDto);
    }

    public ResponseEntity<Object> getItem(long userId, Long itemId) {
        return get(String.format("/%d", itemId), userId);
    }

    public ResponseEntity<Object> getOwnerItems(long userId, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> search(long userId, String text, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> deleteItem(long userId, Long itemId) {
        return delete(String.format("/%d", itemId), userId);
    }
}
