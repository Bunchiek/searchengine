package searchengine.services;

import searchengine.dto.indexing.IndexingStatus;

public interface IndexingService {
    IndexingStatus startIndexing();
    IndexingStatus stopIndexing();
    IndexingStatus indexPage(String url);
}
