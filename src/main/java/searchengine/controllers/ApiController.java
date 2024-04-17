package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingStatus;
import searchengine.dto.searching.SearchResult;
import searchengine.dto.searching.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingStatus> startIndexing(){
        return ResponseEntity.ok(indexingService.startIndexing());
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingStatus> stopIndexing(){
        return ResponseEntity.ok(indexingService.stopIndexing());
    }


    @PostMapping("/indexPage")
    public ResponseEntity<IndexingStatus> indexPage(@RequestParam String url){
        return ResponseEntity.ok(indexingService.indexPage(url));
    }

    @GetMapping("/search")
    public SearchResponse search(@RequestParam String query, @RequestParam(defaultValue = "list") String site){
        List<SearchResult> searchResult = searchService.search(query,site);
        SearchResponse searchTest = new SearchResponse();
        searchTest.setResult(true);
        searchTest.setCount(searchResult.size());
        searchTest.setData(searchResult);
        return searchTest;
    }

}
