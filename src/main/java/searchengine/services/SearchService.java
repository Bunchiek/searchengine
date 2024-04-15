package searchengine.services;

import searchengine.dto.searching.SearchResult;

import java.util.List;

public interface SearchService {
    List<SearchResult> search(String query, String site, Integer offset, Integer limit);
}
