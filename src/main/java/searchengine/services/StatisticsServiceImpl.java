package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositoies.LemmaRepository;
import searchengine.repositoies.PageRepository;
import searchengine.repositoies.SiteRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        List<searchengine.model.Site> siteList = siteRepository.findAll();
        TotalStatistics total = new TotalStatistics();
        total.setSites(siteList.size());
        total.setIndexing(siteList.stream().anyMatch(s->s.getStatus().equals(Status.INDEXING)));
        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        for(Site sites : siteList){
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(sites.getName());
            item.setUrl(sites.getUrl());
            List<Page> pages = pageRepository.findBySite(sites);
            List<Lemma> lemmas = lemmaRepository.findBySite(sites);
            item.setPages(pages.size());
            item.setLemmas(lemmas.size());
            item.setStatus(sites.getStatus().toString());
            item.setError(sites.getLastError());
            Timestamp timestamp = Timestamp.valueOf(sites.getStatusTime());
            item.setStatusTime(timestamp.getTime());
            total.setPages(total.getPages() + pages.size());
            total.setLemmas(total.getLemmas() + lemmas.size());
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
