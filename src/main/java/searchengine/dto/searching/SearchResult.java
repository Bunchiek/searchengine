package searchengine.dto.searching;

import lombok.*;


@Setter
@Getter
@ToString
@Data
public class SearchResult {
    private String uri;
    private String title;
    private String snippet;
    private Float relevance;
}
