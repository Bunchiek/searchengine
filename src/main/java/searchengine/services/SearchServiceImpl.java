package searchengine.services;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
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
        List<Lemma> list = map.keySet().stream()
                .map(lemmaRepository::findByLemma)
                .filter(Objects::nonNull)
                .filter(s-> s.getFrequency() < ((double)pageRepository.findAll().size() / 100) * 90)
                .sorted(Comparator.comparing(Lemma::getFrequency))
                //                .peek(s-> {
//                    for(Index index : s.getIndices()){
//                        System.out.println(index.getPage().getPath());
//                    }
//                })
                .toList();




        List<Index> result;
        result = list.get(0).getIndices().stream().toList();

        List<Index> result2 = new ArrayList<>();

        for(int i = 0; i < list.size(); i++){
            for(Index index : result){
                if(list.get(i).getIndices().contains(index)){
                    result2.add(index);
                }
            }
        }


        System.out.println(result2.size());
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
