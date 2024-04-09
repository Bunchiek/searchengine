package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Site;
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
    public Result startIndexing(){
        return indexingService.startIndexing();
    }
    @GetMapping("/stopIndexing")
    public Result stopIndexing(){
        return indexingService.stopIndexing();
    }


    @PostMapping("/indexPage")
    public Result indexPage(@RequestParam String url){
        return indexingService.indexPage(url);
    }

    @GetMapping("/search")
    public Result search(@RequestParam String query, @RequestParam String site,
                         @RequestParam Integer offset, @RequestParam Integer limit){
        searchService.search(query,site,offset,limit);
        return null;
    }

}
