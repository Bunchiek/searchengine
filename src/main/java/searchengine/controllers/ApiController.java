package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.searching.SearchResult;
import searchengine.dto.searching.SearchTest;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Site;
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
    public SearchTest search(@RequestParam String query){
        String site1 = "test";
        Integer offset1 = 0;
        Integer limit1 = 0;
        List<SearchResult> searchResult = searchService.search(query,site1,offset1,limit1);
        SearchTest searchTest = new SearchTest();
        searchTest.setResult(true);
        searchTest.setCount(searchResult.size());
        searchTest.setData(searchResult);
        System.out.println(query);
        return searchTest;
//        return searchService.search(query,site1,offset1,limit1);
    }

//    , @RequestParam String site,
//    @RequestParam Integer offset, @RequestParam Integer limit

}
