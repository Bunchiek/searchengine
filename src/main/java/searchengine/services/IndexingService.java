package searchengine.services;

import org.springframework.http.ResponseEntity;
import searchengine.dto.indexing.IndexingStatus;

public interface IndexingService {
    ResponseEntity<IndexingStatus> startIndexing();
    ResponseEntity<IndexingStatus> stopIndexing();
    ResponseEntity<IndexingStatus> indexPage(String url);
}
