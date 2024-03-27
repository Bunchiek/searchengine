package searchengine.services;

public interface IndexingService {
    Result startIndexing();
    Result stopIndexing();
    Result indexPage(String url);
}
