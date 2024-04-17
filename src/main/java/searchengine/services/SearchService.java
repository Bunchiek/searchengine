package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.searching.SearchResponse;
import searchengine.dto.searching.SearchResult;

import java.util.List;

public interface SearchService {

    ResponseEntity<SearchResponse> search(String query, String site);
}
