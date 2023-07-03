package ru.pankov.services;

import ru.pankov.dto.api.response.SearchResponse;

import java.util.Map;

public interface SearchService {
    SearchResponse search(Map<String, String> req);
}
