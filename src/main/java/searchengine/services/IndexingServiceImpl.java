package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SiteInfo;
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
import java.util.List;
import java.util.Map;
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
    private final SitesList sitesList;

    @Override
    public Result startIndexing() {
        Result result = new Result();
        List<Site> listOfSites = siteRepository.findAll();
        if (!listOfSites.isEmpty()) {
            for (Site site : listOfSites) {
                if (site.getStatus().equals(Status.INDEXING)) {
                    result.setResult(false);
                    result.setError("Индексация уже запущена");
                    return result;
                }
            }
        }
        for (SiteInfo siteInfo : sitesList.getSiteInfos()) {
            Site siteToDelete = siteRepository.findSiteByUrl(siteInfo.getUrl());
            if (siteToDelete != null) {
                siteRepository.deleteByUrl(siteInfo.getUrl());
            }
            Site siteToIndex = new Site();
            siteToIndex.setStatusTime(LocalDateTime.now());
            siteToIndex.setUrl(siteInfo.getUrl());
            siteToIndex.setName(siteInfo.getName());
            siteToIndex.setStatus(Status.INDEXING);
            siteRepository.save(siteToIndex);
            new Thread(() -> indexing(siteToIndex)).start();
        }
        result.setResult(true);
        return result;
    }

    @Override
    public Result stopIndexing() {
        Result result = new Result();
        result.setResult(false);
        List<Site> listOfSites = siteRepository.findAll();
        for (Site site : listOfSites) {
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
                Site site = siteRepository.findByName(siteUrl.getHost());
                if (site == null) {
                    URL newSite = new URL(url);
                    site = new Site(Status.INDEXED, LocalDateTime.now(), newSite.getProtocol() + "://" + newSite.getHost() + "/", newSite.getHost());
                    siteRepository.save(site);
                }
                populatingTables(siteUrl,site,doc,response);

            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                System.out.println(e.getMessage());
            }
        } else {
            result.setResult(false);
            result.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        return result;
    }

    private void indexing(Site site) {
        List<Page> pagesList;
        Page rootPage = new Page();
        rootPage.setPath(site.getUrl());
        SiteMapGeneratorService siteMapGeneratorService = new SiteMapGeneratorService(rootPage, site, this);
        ForkJoinPool pool = new ForkJoinPool();
        pagesList = pool.invoke(siteMapGeneratorService);
        pagesList.addAll(SiteMapGeneratorService.badResponseSites);
        if (siteRepository.findById(site.getId()).get().getStatus().equals(Status.INDEXING)) {
            siteRepository.updateSiteStatusById(Status.INDEXED, site.getId());
        }
    }

    private Boolean urlChecking(String url) {
        URL siteURL = null;
        try {
            siteURL = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
        }
        for (SiteInfo sites : sitesList.getSiteInfos()) {
            if (sites.getUrl().contains(siteURL.getHost())) {
                return true;
            }
        }
        return false;
    }

    private void populatingTables(URL url, Site site, Document document, Connection.Response response) {
        Page page = pageRepository.findByPath(url.getPath());
        if(page != null){
            pageRepository.delete(page);
        }
        page = new Page(url.getPath(), response.statusCode(), document.html(), site);
        pageRepository.save(page);
        siteRepository.updateSiteSetTimeForId(LocalDateTime.now(), site.getId());
        Map<String, Long> map = LemmaConverter.textToLemma(document.html());
        for (Map.Entry<String, Long> lemmaMap : map.entrySet()) {
            Lemma lemma = lemmaRepository.findByLemma(lemmaMap.getKey());
            if (lemma == null) {
                lemma = new Lemma();
                lemma.setSite(site);
                lemma.setLemma(lemmaMap.getKey());
                lemma.setFrequency(1);
                lemmaRepository.save(lemma);
            } else {
                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);
            }
            Index index = new Index(lemmaMap.getValue().intValue(), page, lemma);
            indexRepository.save(index);
        }
    }
}
