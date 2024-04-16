package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repositoies.IndexRepository;
import searchengine.repositoies.LemmaRepository;
import searchengine.repositoies.PageRepository;
import searchengine.repositoies.SiteRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private ForkJoinPool pool = new ForkJoinPool();
    private LemmaConverter lemmaConverter = new LemmaConverter();
    private final SitesList sitesList;
    private List<Thread> threads = new ArrayList<>();

    @Override
    public Result startIndexing() {
        Result result = new Result();
        List<searchengine.model.Site> listOfSites = siteRepository.findAll();
        Page.urls.clear();
        if (!listOfSites.isEmpty()) {
            for (searchengine.model.Site site : listOfSites) {
                if (site.getStatus().equals(Status.INDEXING)) {
                    result.setResult(false);
                    result.setError("Индексация уже запущена");
                    return result;
                }
            }
        }
        for (Site siteInfo : sitesList.getSites()) {
            searchengine.model.Site siteToDelete = siteRepository.findSiteByUrl(siteInfo.getUrl());
            if (siteToDelete != null) {
                siteRepository.deleteByUrl(siteInfo.getUrl());
            }
            searchengine.model.Site siteToIndex = new searchengine.model.Site();
            siteToIndex.setStatusTime(LocalDateTime.now());
            siteToIndex.setUrl(siteInfo.getUrl());
            siteToIndex.setName(siteInfo.getName());
            siteToIndex.setStatus(Status.INDEXING);
            siteRepository.save(siteToIndex);

//            executor.submit(()->indexing(siteToIndex));
//            executor.shutdown();

            new Thread(() -> indexing(siteToIndex)).start();
        }


        result.setResult(true);
        return result;
    }

    @Override
    public Result stopIndexing() {
        executor.shutdown();
        Result result = new Result();
        result.setResult(false);
        List<searchengine.model.Site> listOfSites = siteRepository.findAll();
        for (searchengine.model.Site site : listOfSites) {
            if (site.getStatus().equals(Status.INDEXING)) {
                siteRepository.updateSiteStatusAndError(Status.FAILED, "Индексация остановлена пользователем", site.getId());
                result.setResult(true);
            }
        }
        if (result.getResult()) {
            return result;
        }
        result.setError("Индексация не запущена");
        return result;
    }

    @Override
    public Result indexPage(String url) {
        Result result = new Result();
        if (urlChecking(url)) {
            result.setResult(true);
            try {
                Connection.Response response = Jsoup.connect(url).execute();
                Document doc = response.parse();
                URL siteUrl = new URL(url);
                searchengine.model.Site site = siteRepository.findByName(siteUrl.getHost());
                if (site == null) {
                    site = new searchengine.model.Site(Status.INDEXED, LocalDateTime.now(), siteUrl.getProtocol() + "://" + siteUrl.getHost() + "/", siteUrl.getHost());
                    siteRepository.save(site);
                }
                populatingTables(siteUrl, site, doc, response);

            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
            }
        } else {
            result.setResult(false);
            result.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return result;
    }

    private void indexing(searchengine.model.Site site) {
        long start = System.currentTimeMillis();
        Page rootPage = new Page();
        rootPage.setPath(site.getUrl());
        SiteMapGeneratorService siteMapGeneratorService = new SiteMapGeneratorService(rootPage, site, pageRepository, siteRepository, lemmaRepository, indexRepository);
//        SiteMapGeneratorService siteMapGeneratorService = new SiteMapGeneratorService();
        pool = new ForkJoinPool();
        pool.invoke(siteMapGeneratorService);
        pool.shutdown();
        if (siteRepository.findById(site.getId()).get().getStatus().equals(Status.INDEXING)) {
            siteRepository.updateSiteStatusById(Status.INDEXED, site.getId());
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - start);
    }

    private Boolean urlChecking(String url) {
        URL siteURL = null;
        try {
            siteURL = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
        for (Site sites : sitesList.getSites()) {
            if (sites.getUrl().contains(siteURL.getHost())) {
                return true;
            }
        }
        return false;
    }

    private void populatingTables(URL url, searchengine.model.Site site, Document document, Connection.Response response) {
        Page page = pageRepository.findFirstByPath(url.getPath());
        if(page != null){
            pageRepository.delete(page);
        }
        page = new Page(url.getPath(), response.statusCode(), document.html(), site);
        pageRepository.save(page);
        siteRepository.updateSiteSetTimeForId(LocalDateTime.now(), site.getId());
        Map<String, Long> map = lemmaConverter.textToLemma(document.html());
//        for (Map.Entry<String, Long> lemmaMap : map.entrySet()) {
//            Lemma lemmaList = lemmaRepository.findByLemmaAndSite(lemmaMap.getKey(),site);
//            Lemma lemma = new Lemma();
//            if (lemmaList.isEmpty()) {
//                lemma.setSite(site);
//                lemma.setLemma(lemmaMap.getKey());
//                lemma.setFrequency(1);
//                lemmaRepository.save(lemma);
//            } else {
//                lemmaList.stream()
//                                .forEach(s->{
//                                    lemmaRepository.deleteByLemma(s.getLemma());
//                                    lemma.setSite(s.getSite());
//                                    lemma.setLemma(s.getLemma());
//                                    lemma.setFrequency(lemma.getFrequency()+s.getFrequency());
//                                });
//                lemma.setFrequency(lemma.getFrequency() + 1);
//                lemmaRepository.save(lemma);
//            }
//            Index index = new Index(lemma, page, lemmaMap.getValue().intValue());
//            indexRepository.save(index);
//        }
    }
}
