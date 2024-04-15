package searchengine.dto.searching;

import lombok.*;

@Data
public class SearchResult {
    private String site = "test";
    private String siteName = "test";
    private String uri;
    private String title;
    private String snippet;
    private Float relevance;
}
