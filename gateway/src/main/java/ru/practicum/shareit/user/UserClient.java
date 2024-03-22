package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.common.BaseClient;
import ru.practicum.shareit.user.dto.UserRequest;

import java.util.Map;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> add(UserRequest userRequest) {
        return post("", userRequest);
    }

    public ResponseEntity<Object> getByUserId(long userId) {
        return get("/{userId}", null, Map.of("userId", Long.toString(userId)));
    }

    public ResponseEntity<Object> getAll() {
        return get("");
    }

    public ResponseEntity<Object> update(long userId, UserRequest userRequest) {
        return patch("/" + userId, userRequest);
    }

    public ResponseEntity<Object> delete(long userId) {
        return delete("/{userId}", null, Map.of("userId", Long.toString(userId)));
    }
}
