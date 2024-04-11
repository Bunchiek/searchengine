package searchengine.dto.searching;

import lombok.Data;

@Data
public class SearchResult {
    private String uri;
    private String title;
    private String snippet;
    private Float relevance;
}
