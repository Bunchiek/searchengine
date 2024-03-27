package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LemmaConverter {
    public static Map<String, Long> textToLemma(String html) {
        Map<String, Long> map;
        String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            String result = Jsoup.clean(html, Safelist.none());
            result = result.replaceAll("[^А-Яа-я]", " ").replaceAll("\\s{2,}", " ");
            map = Stream.of(result.split(" "))
                    .map(String::toLowerCase)
                    .filter(s -> {
                        for (String string : particlesNames) {
                            if (luceneMorph.getMorphInfo(s).toString().contains(string)) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .map(s -> luceneMorph.getNormalForms(s).get(0))
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}
