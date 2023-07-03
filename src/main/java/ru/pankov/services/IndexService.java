package ru.pankov.services;

import ru.pankov.dto.api.request.IndexRequest;
import ru.pankov.dto.api.response.IndexResponse;

public interface IndexService {
    IndexResponse indexAll();
    IndexResponse stopAll();
    IndexResponse indexSinglePage(IndexRequest indexRequest);
}
