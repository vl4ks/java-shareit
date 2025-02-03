package ru.practicum.shareit.request.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String GET_ALL_ITEMS = "/all";
    private static final String REQUESTS = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + REQUESTS))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build());
    }

    public ResponseEntity<Object> createItemRequest(Long userId, ItemRequestSaveDto itemRequestSaveDto) {
        return post("", userId, itemRequestSaveDto);
    }

    public ResponseEntity<Object> getAllUserItemRequest(Long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllItemRequests(Long userId) {
        return get(GET_ALL_ITEMS, userId);
    }

    public ResponseEntity<Object> getItemRequest(Long requestId) {
        return get("/" + requestId);
    }

}
