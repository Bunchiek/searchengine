package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Service

public class LemmaConverter {
    public Map<String, Long> textToLemma(String html) {
        Map<String, Long> map;
        List<String> list = List.of("МЕЖД", "ПРЕДЛ", "СОЮЗ");

        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            String result = Jsoup.clean(html, Safelist.none());
            result = result.replaceAll("[^А-Яа-я]", " ").replaceAll("\\s{2,}", " ");
            map = Stream.of(result.split(" "))
                    .map(String::toLowerCase)
                    .filter(Predicate.not(String::isEmpty))
                    .filter(luceneMorph::checkString)
                    .filter(s -> {
                        for (String string : list) {
                            if (luceneMorph.getMorphInfo(s).toString().contains(string)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .map(s->luceneMorph.getMorphInfo(s).get(0))
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}
