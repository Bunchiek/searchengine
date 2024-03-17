package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteInfo;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositoies.PageRepository;
import searchengine.repositoies.SiteRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SitesList sitesList;
    @Override
    public void startIndexing() {
        List<Site> listToDelete = new ArrayList<>();
        for(SiteInfo siteInfo : sitesList.getSiteInfos()){
            for(Site siteToDelete : siteRepository.findAll()){
                if(siteInfo.getUrl().equals(siteToDelete.getUrl())){
                    listToDelete.add(siteToDelete);
                }
            }
            siteRepository.deleteAll(listToDelete);
            Site siteToIndex = new searchengine.model.Site();
            siteToIndex.setStatusTime(LocalDateTime.now());
            siteToIndex.setUrl(siteInfo.getUrl());
            siteToIndex.setName(siteInfo.getName());
            siteToIndex.setStatus(Status.INDEXING);
            siteRepository.save(siteToIndex);
            new Thread(()-> indexing(siteInfo.getUrl(), siteToIndex)).start();

        }
    }

    private void indexing(String url, Site site){
        List<Page> pagesList;
        Page rootPage = new Page();
        rootPage.setPath(url);
        SiteMapGeneratorService siteMapGeneratorService = new SiteMapGeneratorService(rootPage,site,pageRepository);
        ForkJoinPool pool = new ForkJoinPool();
        pagesList = pool.invoke(siteMapGeneratorService);
        pagesList.addAll(SiteMapGeneratorService.badResponseSites);
        for(Page pages : pagesList){
            pages.setSite(site);
        }
    }

}
