package searchengine.services;

public interface SearchService {
    Result search(String query, String site, Integer offset, Integer limit);
}
