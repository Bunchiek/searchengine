package searchengine.services;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositoies.IndexRepository;
import searchengine.repositoies.LemmaRepository;
import searchengine.repositoies.PageRepository;
import searchengine.repositoies.SiteRepository;

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
        Map<String, Long> map = lemmaConverter.textToLemma(query);
        System.out.println(map);
        List<Lemma> list = map.keySet().stream()
                .map(s->lemmaRepository.findByLemma(s).get(0))
                .takeWhile(Objects::nonNull)
                .filter(Objects::nonNull)
                .filter(s-> s.getFrequency() < ((double)pageRepository.findAll().size() / 100) * 90)
                .sorted(Comparator.comparing(Lemma::getFrequency))
                .toList();

        if(list.isEmpty()){
            System.out.println("kek");
            return null;
        }
        List<Page> result;
        result = list.get(0).getIndices()
                .stream()
                .map(Index::getPage)
                .toList();

        List<Page> result2 = new ArrayList<>(result);

        if(list.size()==1){

        }else{
            for(int i = 1; i < list.size(); i++){
                for(Page page : result){
                    if(!list.get(i).getIndices().stream().map(Index::getPage).toList().contains(page))
                        result2.clear();
                    System.out.println("kek1");
                }
            }
        }
        System.out.println(result2);

        Float rel = result2.stream()
                .map(s->{
                    float result3 = (float) 0;
                    for(Lemma lemma : list){
                        result3 += indexRepository.findIndexByPageAndLemma(s,lemma).getRank();
                    }
                    return result3;
                })
                .peek(System.out::println)
                .max(Comparator.comparing(Float::valueOf)).orElseThrow();

        System.out.println(rel);

        List<Page> test = result2.stream()
                .sorted(Comparator.comparing(s->{
                    float resul = (float)0;
                    for(Lemma lemma : list){
                    resul += indexRepository.findIndexByPageAndLemma(s,lemma).getRank();
                }
                    return resul;}))
                .toList();
        System.out.println(test);

        return null;
    }


    private List<Index> test (List<Index> indexes, Lemma lemma){
        for(Index index : indexes){
            for(Index lemmaIndex : lemma.getIndices()){
                if(lemmaIndex == index){
                    indexes.remove(index);
                }
            }
        }
        return indexes;
    }
}
