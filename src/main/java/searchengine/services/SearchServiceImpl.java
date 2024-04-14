package searchengine.services;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.searching.SearchResult;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositoies.IndexRepository;
import searchengine.repositoies.LemmaRepository;
import searchengine.repositoies.PageRepository;
import searchengine.repositoies.SiteRepository;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaConverter lemmaConverter;
    @Override
    public Result search(String query, String site, Integer offset, Integer limit) {
        Map<String, Integer> map = null;
        try {
            map = LemmaFinder.getInstance().collectLemmas(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Слова что ты ввел" + map);

        List<Lemma> list = map.keySet().stream()
                .map(s->lemmaRepository.findByLemma(s).get(0))
                .takeWhile(Objects::nonNull)
                .filter(Objects::nonNull)
                .filter(s-> s.getFrequency() < ((double)pageRepository.findAll().size() / 100) * 90)
                .sorted(Comparator.comparing(Lemma::getFrequency))
                .toList();


        System.out.println("Леммы полученные из списка слов " + list);

        if(list.isEmpty()){
            System.out.println("ничего не найдено1");
            return null;
        }

        List<Page> result = list.get(0).getIndices()
                .stream()
                .map(Index::getPage)
                .toList();

        System.out.println("список всех страниц где есть слово" + result);

        List<Page> result2 = new ArrayList<>(result);

        if(list.size()==1){
            System.out.println("Единственное слово и его страницы " + result);

        }else{
            for(int i = 1; i < list.size(); i++){
                for(Page page : result){
                    Set<Index> set = list.get(i).getIndices();
                    List<Page> pages = new ArrayList<>();
                    for(Index set1 : set){
                        pages.add(set1.getPage());
                    }
                    if(!pages.contains(page)){
                        result2.remove(page);
                    }
//                    if(!(list.get(i).getIndices().stream().map(Index::getPage).toList().contains(page)))
//                        result2.clear();
//                    return null;
                }
                if(result2.isEmpty()){
                    return null;
                }
            }
        }

        System.out.println("найденные страницы" + result2);

        Float maxAbsRel = result2.stream()
                .map(s->{
                    float result3 = (float) 0;
                    for(Lemma lemma : list){
                        result3 += indexRepository.findIndexByPageAndLemma(s,lemma).getRank();
                    }
                    return result3;
                })
                .max((Comparator.comparing(Float::valueOf))).orElseThrow();

        System.out.println("макс значение абсолютной релевантности" + maxAbsRel);



//        List<SearchResult> test = result2.stream()
//                .sorted(Comparator.comparing(s->{
//                    float resul = (float)0;
//                    for(Lemma lemma : list){
//                    resul += indexRepository.findIndexByPageAndLemma((Page) s,lemma).getRank();
//                }
//                    resul /= maxAbsRel;
//                    searchResult.setRelevance(resul);
//                    return resul;}).reversed())
//                .map(s->{
//                    searchResult.setUri(s.getPath());
//                    Document document = Jsoup.parse(s.getContent());
//                    searchResult.setTitle(document.title());
//                    searchResult.setSnippet("DEVELOPING");
//                    return searchResult;
//                })
//                .toList();


        List<SearchResult> test2 = new ArrayList<>();

        for(Page page : result2){
            float reer = 0.0F;
            SearchResult searchResult = new SearchResult();
            searchResult.setUri(page.getPath());
            Document document = Jsoup.parse(page.getContent());
            searchResult.setTitle(document.title());
            searchResult.setSnippet("Developing");
            for(Lemma lemma : list){
                reer += indexRepository.findIndexByPageAndLemma(page,lemma).getRank()/maxAbsRel;
            }
            searchResult.setRelevance(reer/maxAbsRel);
            test2.add(searchResult);
        }

        test2.sort(Comparator.comparing(SearchResult::getRelevance).reversed());



        System.out.println("Список страниц отсортированных по абсолютной релевантности" + test2);
        return null;
    }

}
