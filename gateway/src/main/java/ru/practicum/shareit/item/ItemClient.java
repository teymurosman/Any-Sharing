package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.BaseClient;
import ru.practicum.shareit.item.dto.CommentFromRequest;
import ru.practicum.shareit.item.dto.ItemFromRequest;

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

    public ResponseEntity<Object> add(long userId, ItemFromRequest itemFromRequest) {
        return post("", userId, itemFromRequest);
    }

    public ResponseEntity<Object> getByItemId(long userId, Long itemId) {
        return get("/{itemId}", userId, Map.of("itemId", itemId));
    }

    public ResponseEntity<Object> getAllByOwnerId(long userId, int from, int size) {
        return get("?from={from}&size={size}", userId, Map.of("from", from, "size", size));
    }

    public ResponseEntity<Object> update(long userId, Long itemId, ItemFromRequest itemFromRequest) {
        return patch("/{itemId}", userId, Map.of("itemId", itemId), itemFromRequest);
    }

    public ResponseEntity<Object> search(String text, int from, int size) {
        return get("/search?text={text}&from={from}&size={size}",
                null,
                Map.of("text", text, "from", from, "size", size));
    }

    public ResponseEntity<Object> addComment(long userId, Long itemId, CommentFromRequest commentFromRequest) {
        return post("/{itemId}/comment", userId, Map.of("itemId", itemId), commentFromRequest);
    }
}
