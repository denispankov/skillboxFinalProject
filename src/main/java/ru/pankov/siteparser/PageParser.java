package ru.pankov.siteparser;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class PageParser {


    static public Page parse(String url) {
        List<String> pageLinks = new ArrayList<>();
        int statusCode = 200;
        String content = "";
        String titleLemmas = "";
        String contentLemmas = "";
        try {
            Response response = Jsoup.connect(url).execute();
            statusCode = response.statusCode();
            content = response.body();
            Document doc = response.parse();
            titleLemmas = doc.title();
            contentLemmas = doc.body().text();
            Elements links = doc.select("a");
            links.forEach(link -> pageLinks.add(link.absUrl("href").replaceAll("#.*$", "")));
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return new Page(pageLinks, statusCode, content, url, contentLemmas, titleLemmas);
    }
}
