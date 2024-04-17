package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingStatus;
import searchengine.dto.searching.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.*;

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
        return indexingService.startIndexing();
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingStatus> stopIndexing(){
        return indexingService.stopIndexing();
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingStatus> indexPage(@RequestParam String url){
        return indexingService.indexPage(url);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam String query, @RequestParam(defaultValue = "list") String site){
        return searchService.search(query,site);

    }

}
