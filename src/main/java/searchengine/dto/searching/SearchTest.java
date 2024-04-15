package searchengine.dto.searching;

import lombok.Data;

import java.util.List;

@Data
public class SearchTest {
    private boolean result;
    private int count;
    private List<SearchResult> data;
}
