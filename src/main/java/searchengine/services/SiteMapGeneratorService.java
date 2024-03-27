package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositoies.IndexRepository;
import searchengine.repositoies.LemmaRepository;
import searchengine.repositoies.PageRepository;
import searchengine.repositoies.SiteRepository;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.RecursiveTask;

@Service
@RequiredArgsConstructor
public class SiteMapGeneratorService extends RecursiveTask<List<Page>> {
    private Page page;
    private Site site;
    private IndexingServiceImpl indexingService;
    protected static List<Page> badResponseSites = new ArrayList<>();

    public SiteMapGeneratorService(Page page, Site site, IndexingServiceImpl indexingService) {
        this.page = page;
        this.site = site;
        this.indexingService  = indexingService;
    }

    @Override
    protected List<Page> compute() {
        String url = page.getPath();
        List<Page> list = webTree(url);
        List<SiteMapGeneratorService> subTask = new LinkedList<>();
        for (Page page : list) {
            SiteMapGeneratorService task = new SiteMapGeneratorService(page, site, indexingService);
            task.fork();
            subTask.add(task);
            this.page.addChild(page);
        }
        for (SiteMapGeneratorService task : subTask) {
            list.addAll(task.join());
        }
        return list;
    }

    private List<Page> webTree(String url) {
        Document doc = null;
        List<Page> list = new ArrayList<>();
        String temp = "";
        try {
            Connection.Response response = Jsoup.connect(url).execute();
            if (200 == response.statusCode()) {
                doc = response.parse();
                Elements elements = doc.select("a");
                for (Element element : elements) {
                    temp = element.attr("abs:href");
                    if (!temp.endsWith("/")) {
                        temp = temp + "/";
                    }
                    if (temp.contains(url) && !temp.contains("#")) {
                        URL path = new URL(temp);
                        Page page = indexingService.getPageRepository().findByPath(path.getPath());
                        if (page == null) {
                            list = savePage(temp, doc, path);
                        }
                    }
                }
            }
        } catch (HttpStatusException e) {
            badResponseSites.add((new Page(temp, e.getStatusCode(), "Ошибка чтения страницы", site)));
        } catch (IOException | InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }

    private List<Page> savePage(String path, Document document, URL url) throws IOException, InterruptedException {
        List<Page> result = new ArrayList<>();
        Thread.sleep(200);
        Connection.Response response = Jsoup.connect(path).execute();
        if (200 == response.statusCode()) {
            document = response.parse();
            result.add(new Page(path, response.statusCode(), document.html(), site));
            indexingService.indexPage(path);
            indexingService.getSiteRepository().updateSiteSetTimeForId(LocalDateTime.now(), site.getId());
            if (indexingService.getSiteRepository().findById(site.getId()).get().getStatus().equals(Status.FAILED)) {
                Thread.currentThread().interrupt();
            }
        }
        return result;
    }
}

