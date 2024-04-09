package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.model.*;
import searchengine.repositoies.IndexRepository;
import searchengine.repositoies.LemmaRepository;
import searchengine.repositoies.PageRepository;
import searchengine.repositoies.SiteRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

@Service
@RequiredArgsConstructor
public class SiteMapGeneratorService extends RecursiveAction {
    private Page page;
    private Site site;
    private PageRepository pageRepository;
    private SiteRepository siteRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;
    private final LemmaConverter lemmaConverter = new LemmaConverter();

    public SiteMapGeneratorService(Page page, Site site, PageRepository pageRepository, SiteRepository siteRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.page = page;
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    @Override
    protected void compute() {
        List<Page> list = webTree(page);
        List<SiteMapGeneratorService> subTask = new LinkedList<>();
        for (Page page : list) {
            SiteMapGeneratorService task = new SiteMapGeneratorService(page, site, pageRepository, siteRepository, lemmaRepository, indexRepository);
            task.fork();
            subTask.add(task);
            this.page.addChild(page);
        }
        for (SiteMapGeneratorService task : subTask) {
            task.join();
        }
    }

    private  List<Page> webTree(Page page) {
        Document doc = null;
        List<Page> list = new ArrayList<>();
        String temp = "";
        try {
            URL path = new URL(page.getPath());
            Thread.sleep(1000);
            Connection.Response response = Jsoup.connect(page.getPath()).execute();
            if (200 == response.statusCode()) {
                doc = response.parse();
                page.setPath(path.getPath());
                page.setSite(site);
                page.setCode(response.statusCode());
                page.setContent(doc.html());
                testSave(page);
                siteRepository.updateSiteSetTimeForId(LocalDateTime.now(), site.getId());
                Elements elements = doc.select("a");
                for (Element element : elements) {
                    temp = element.attr("abs:href");
                    if (!temp.endsWith("/")) {
                        temp = temp + "/";
                    }
                    if (temp.contains(path.getHost()) && !temp.contains("#")) {
                        if (!Page.urls.contains(temp)) {
                            Page.urls.add(temp);
                            list.add(new Page(temp));
                        }
                    }
                }
            }
        } catch (HttpStatusException ignored) {
//            System.out.println(ignored.getStatusCode() + " " + temp);
        } catch (IOException | InterruptedException | ArrayIndexOutOfBoundsException ex) {
            System.out.println(ex.getMessage() + " " + temp);
        }
        return list;
    }

    private  void testSave(Page page) {
        if (pageRepository.findFirstByPath(page.getPath()) == null) {
            pageRepository.save(page);
            populatingTable(page);
        }
    }
    private  void populatingTable(Page page) {
        Map<String, Long> map = lemmaConverter.textToLemma(page.getContent());
        for (Map.Entry<String, Long> lemmaMap : map.entrySet()) {
            Lemma lemma = lemmaRepository.findFirstByLemmaAndSite(lemmaMap.getKey(), site);
            if (lemma == null) {
                lemma = new Lemma();
                lemma.setSite(site);
                lemma.setLemma(lemmaMap.getKey());
                lemma.setFrequency(1);
                lemmaRepository.save(lemma);
            } else {
                lemmaRepository.updateLemmaFrequency(lemma.getFrequency()+1,lemma.getId());
            }
            Index index = new Index(lemma, page, lemmaMap.getValue().intValue());
            indexRepository.save(index);
        }
    }
}

