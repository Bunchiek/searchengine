package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingStatus;
import searchengine.model.*;
import searchengine.repositoies.IndexRepository;
import searchengine.repositoies.LemmaRepository;
import searchengine.repositoies.PageRepository;
import searchengine.repositoies.SiteRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private ForkJoinPool pool = new ForkJoinPool();
    private final SitesList sitesList;
    static Lock lock = new ReentrantLock();

    @Override
    public ResponseEntity<IndexingStatus> startIndexing() {
        IndexingStatus indexingStatus = new IndexingStatus();
        List<searchengine.model.Site> listOfSites = siteRepository.findAll();
        Page.urls.clear();
        if (!listOfSites.isEmpty()) {
            for (searchengine.model.Site site : listOfSites) {
                if (site.getStatus().equals(Status.INDEXING)) {
                    indexingStatus.setResult(false);
                    indexingStatus.setError("Индексация уже запущена");
                    return new ResponseEntity<>(indexingStatus, HttpStatus.BAD_REQUEST);
                }
            }
        }
        for (Site site : sitesList.getSites()) {
            searchengine.model.Site siteToDelete = siteRepository.findSiteByUrl(site.getUrl());
            if (siteToDelete != null) {
                siteRepository.deleteByUrl(site.getUrl());
            }
            searchengine.model.Site siteToIndex = new searchengine.model.Site();
            siteToIndex.setStatusTime(LocalDateTime.now());
            siteToIndex.setUrl(site.getUrl());
            siteToIndex.setName(site.getName());
            siteToIndex.setStatus(Status.INDEXING);
            siteRepository.save(siteToIndex);
            new Thread(() -> indexing(siteToIndex)).start();
        }
        indexingStatus.setResult(true);
        return new ResponseEntity<>(indexingStatus, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<IndexingStatus> stopIndexing() {
        IndexingStatus indexingStatus = new IndexingStatus();
        indexingStatus.setResult(false);
        List<searchengine.model.Site> listOfSites = siteRepository.findAll();
        for (searchengine.model.Site site : listOfSites) {
            if (site.getStatus().equals(Status.INDEXING)) {
                siteRepository.updateSiteStatusAndError(Status.FAILED, "Индексация остановлена пользователем", site.getId());
                indexingStatus.setResult(true);
            }
        }
        if (indexingStatus.getResult()) {
            return new ResponseEntity<>(indexingStatus, HttpStatus.OK);
        }
        indexingStatus.setError("Индексация не запущена");
        return new ResponseEntity<>(indexingStatus, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<IndexingStatus> indexPage(String url) {
        IndexingStatus indexingStatus = new IndexingStatus();
        UrlValidator validator = new UrlValidator();
        if(!validator.isValid(url)){
            indexingStatus.setResult(false);
            indexingStatus.setError("Неверный ввод страницы");
            return new ResponseEntity<>(indexingStatus, HttpStatus.BAD_REQUEST);
        }
        if (urlChecking(url)) {
            indexingStatus.setResult(true);
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
            indexingStatus.setResult(false);
            indexingStatus.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
            return new ResponseEntity<>(indexingStatus, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(indexingStatus, HttpStatus.OK);
    }

    private void indexing(searchengine.model.Site site) {
        long start = System.currentTimeMillis();
        Page rootPage = new Page();
        rootPage.setPath(site.getUrl());
        SiteMapGeneratorService siteMapGeneratorService = new SiteMapGeneratorService(rootPage, site, pageRepository, siteRepository, lemmaRepository, indexRepository);
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
        if (page != null) {
            pageRepository.delete(page);
        }
        page = new Page(url.getPath(), response.statusCode(), document.html(), site);
        pageRepository.save(page);
        siteRepository.updateSiteSetTimeForId(LocalDateTime.now(), site.getId());
        Map<String, Integer> map;
        try {
            map = LemmaFinder.getInstance().collectLemmas(page.getContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<String, Integer> lemmaMap : map.entrySet()) {
            lock.lock();
            Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaMap.getKey(), site);
            if (lemma == null) {
                lemma = new Lemma();
                lemma.setSite(site);
                lemma.setLemma(lemmaMap.getKey());
                lemma.setFrequency(1);
                lemmaRepository.save(lemma);
            } else {
                lemmaRepository.updateLemmaFrequency(lemma.getFrequency() + 1, lemma.getId());
            }
            lock.unlock();
            Index index = new Index(lemma, page, lemmaMap.getValue());
            indexRepository.save(index);
        }
    }
}
