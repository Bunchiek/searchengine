package searchengine.services;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositoies.PageRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class SiteMapGeneratorService extends RecursiveTask<List<Page>> {
    private Page page;
    private Site site;
    private PageRepository pageRepository;
    protected static List<Page> badResponseSites = new ArrayList<>();
    public SiteMapGeneratorService(Page page, Site site, PageRepository repository) {
        this.page = page;
        this.site = site;
        this.pageRepository = repository;
    }

    @Override
    protected List<Page> compute() {
        String url = page.getPath();
        List<Page> list = webTree(url);
        List<SiteMapGeneratorService> subTask = new LinkedList<>();
        for (Page page : list) {
            SiteMapGeneratorService task = new SiteMapGeneratorService(page, site, pageRepository);
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
                        List<Page> pageList = pageRepository.findByPath(temp.substring(temp.indexOf("/",8)));
                        if (pageList.isEmpty()) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            Connection.Response response2 = Jsoup.connect(temp).execute();
                            if (200 == response2.statusCode()) {
                                doc = response2.parse();
                                list.add(new Page(temp, response2.statusCode(), doc.text(), site));
                                pageRepository.save(new Page(temp.substring(temp.indexOf("/",8)), response2.statusCode(), doc.text(), site));
                            }
                        }
                    }
                }
            }
        } catch (HttpStatusException e) {
            badResponseSites.add((new Page(temp, e.getStatusCode(), "Ошибка чтения страницы", site)));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return list;
    }
}

