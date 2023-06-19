package ru.pankov.services.siteparser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.dto.siteparser.Page;
import ru.pankov.entities.SiteEntity;
import ru.pankov.services.PageService;

@Service
public class PageIndexerService {

    @Autowired
    private PageParserService pageParser;

    @Autowired
    private PageService pageService;

    public Page indexPage(String pageLink, SiteEntity siteEntity, boolean pageCouldBeenIndexed){
        Page newPage = pageParser.parse(pageLink);
        newPage.setSiteEntity(siteEntity);
        newPage.setRelativePageLink(pageLink.replaceAll(siteEntity.getUrl(), ""));

        if (pageCouldBeenIndexed){
            pageService.deletePage((newPage.getRelativePageLink()));
        }

        pageService.saveIndexPage(newPage);

        return newPage;
    }


}
