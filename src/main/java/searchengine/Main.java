package searchengine;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class Main {
    public static void main(String[] args) throws IOException {

        LuceneMorphology luceneMorph = new RussianLuceneMorphology();

        String test = "ыфвфывфыв";

        System.out.println(luceneMorph.checkString(test));
        System.out.println(luceneMorph.getMorphInfo(test));
//
//
//        String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
//
//        String text = "https://et-cetera.ru/mobile/performance/";

//        ;
//        System.out.println(luceneMorph.checkString(test));
//        System.out.println(luceneMorph.getMorphInfo(test));

//        URL url = new URL(text);
//
//        System.out.println(url.getProtocol() + "://" + url.getHost() +"/");
//        System.out.println(((double) 23/100) * 90);







//        String result = Jsoup.clean(text, Safelist.none());
//        result = result.replaceAll("[^А-Яа-я]", " ").replaceAll("\\s{2,}", " ");


//        Map<String, Long> map = Stream.of(result.split(" "))
//                .map(String::toLowerCase)
//                .filter(s -> {
//                    for (String string : particlesNames) {
//                        if (luceneMorph.getMorphInfo(s).toString().contains(string)) {
//                            return false;
//                        }
//                    }
//                    return true;
//                })
//                .map(s->luceneMorph.getNormalForms(s).get(0))
//                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
//
//        for(Map.Entry<String,Long> entry : map.entrySet()){
//            System.out.println(entry);
//        }

    }
}

