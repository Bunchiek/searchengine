package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.searching.SearchResponse;
import searchengine.dto.searching.SearchResult;
import searchengine.model.*;
import searchengine.repositoies.IndexRepository;
import searchengine.repositoies.LemmaRepository;
import searchengine.repositoies.PageRepository;
import searchengine.repositoies.SiteRepository;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{

    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;

    @Override
    public ResponseEntity<SearchResponse> search(String query, String site) {
        SearchResponse searchResponse = new SearchResponse();

        if(query.isEmpty()){
            searchResponse.setResult(false);
            searchResponse.setError("Задан пустой поисковый запрос");
            return new ResponseEntity<>(searchResponse, HttpStatus.BAD_REQUEST);
        }else {
            List<SearchResult> searchResultList = new ArrayList<>();
            if(site.equals("list")){
                for(Site sites : siteRepository.findAll()){
                    if(sites.getStatus().equals(Status.INDEXED)){
                        searchResultList.addAll(getPages(query,sites));
                    }
                }
            }else {
                if(siteRepository.findSiteByUrl(site).getStatus().equals(Status.INDEXED)){
                    searchResultList.addAll(getPages(query,siteRepository.findSiteByUrl(site)));
                }else {
                    searchResponse.setResult(false);
                    searchResponse.setError("Сайт не проиндексирован");
                    return new ResponseEntity<>(searchResponse, HttpStatus.BAD_REQUEST);
                }

            }
            if(searchResultList.isEmpty()){
                searchResponse.setResult(false);
                searchResponse.setError("Ничего не найдено");
                return new ResponseEntity<>(searchResponse,HttpStatus.NOT_FOUND);
            }
            searchResponse.setResult(true);
            searchResponse.setCount(searchResultList.size());
            searchResponse.setData(searchResultList);
        }
        return ResponseEntity.ok(searchResponse);
    }

    private String getSnippet(String content, String query){
        content = Jsoup.clean(content, Safelist.none());
        String[] queryArray = query.split(" ");
        int start = content.indexOf(queryArray[0]);
        int finish = start;
        if(start == -1){
            return query;
        }
        while (content.charAt(start - 1) != '.') {
            start--;
        }
        for(;;){
            if(content.charAt(finish + 1) == '.'){
                finish = finish + 2;
                break;
            }
            finish ++;
        }
        content = content.substring(start, finish).replaceAll("[^А-Я а-я.,]", "");

        for(String word : queryArray){
            if(word.length()>3){
                content = content.replaceAll(word,"<b>"+word+"</b>");
            }
        }
        return content.trim();
    }

    private List<SearchResult> fillingList(List<Page> pageList, List<Lemma> lemmaList, List<SearchResult> searchResults, String query){
        float maxAbsRel = pageList.stream()
                .map(s->{
                    float result3 = (float) 0;
                    for(Lemma lemma : lemmaList){
                        result3 += indexRepository.findIndexByPageAndLemma(s,lemma).getRank();
                    }
                    return result3;
                })
                .max((Comparator.comparing(Float::valueOf))).orElseThrow();
        for(Page page : pageList){
            float reer = 0.0F;
            SearchResult searchResult = new SearchResult();
            searchResult.setUri(page.getPath());
            Document document = Jsoup.parse(page.getContent());
            searchResult.setTitle(document.title());
            searchResult.setSnippet(getSnippet(page.getContent(), query));
            for(Lemma lemma : lemmaList){
                reer += indexRepository.findIndexByPageAndLemma(page,lemma).getRank()/maxAbsRel;
            }
            Site site = page.getSite();
            searchResult.setSite(site.getUrl());
            searchResult.setSiteName(site.getName());
            searchResult.setRelevance(reer/maxAbsRel);
            searchResults.add(searchResult);
        }
        searchResults.sort(Comparator.comparing(SearchResult::getRelevance).reversed());
        return searchResults;
    }

    private List<SearchResult> getPages(String query, Site site){
        List<SearchResult> result = new ArrayList<>();
        Map<String, Integer> map = null;
        try {
            map = LemmaFinder.getInstance().collectLemmas(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Lemma> lemmaList = map.keySet().stream()
                .map(s->lemmaRepository.findByLemmaAndSite(s,site))
                .takeWhile(Objects::nonNull)
                .filter(Objects::nonNull)
                .filter(s-> s.getFrequency() < ((double)pageRepository.findAll().size() / 100) * 90)
                .sorted(Comparator.comparing(Lemma::getFrequency))
                .toList();

        if(lemmaList.isEmpty()){
            return result;
        }

        List<Page> listOfAllPages = lemmaList.get(0).getIndices()
                .stream()
                .map(Index::getPage)
                .toList();

        List<Page> listSortedPages = new ArrayList<>(listOfAllPages);
        if(lemmaList.size()==1){
            return fillingList(listOfAllPages,lemmaList,result, query);
        }else{
            for(int i = 1; i < lemmaList.size(); i++){
                for(Page page : listOfAllPages){
                    Set<Index> set = lemmaList.get(i).getIndices();
                    List<Page> pages = new ArrayList<>();
                    for(Index set1 : set){
                        pages.add(set1.getPage());
                    }
                    if(!pages.contains(page)){
                        listSortedPages.remove(page);
                    }
                }
                if(listSortedPages.isEmpty()){
                    return result;
                }
            }
        }
        return fillingList(listSortedPages,lemmaList, result, query);
    }
}
