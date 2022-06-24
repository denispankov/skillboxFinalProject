package ru.pankov.siteparser;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class PageParser {


    static public Page getAllLinks(String url) {
        List<String> pageLinks = new ArrayList<>();
        int statusCode = 200;
        String content = "";
        try {
            Response response = Jsoup.connect(url).execute();
            statusCode = response.statusCode();
            content = response.body();
            Document doc = response.parse();
            Elements links = doc.select("a");
            links.forEach(link -> pageLinks.add(link.absUrl("href").replaceAll("#.*$", "")));
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return new Page(pageLinks, statusCode, content, url);
    }
}
