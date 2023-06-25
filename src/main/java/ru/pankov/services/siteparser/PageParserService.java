package ru.pankov.services.siteparser;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.dto.lemmanization.Lemma;
import ru.pankov.dto.siteparser.Page;
import ru.pankov.services.lemmanization.LemmatizerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PageParserService {
    private LemmatizerService lemmatizer;

    @Autowired
    public void setLemmatizer(LemmatizerService lemmatizer) {
        this.lemmatizer = lemmatizer;
    }

    public Page parse(String url) {
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
        }

        return new Page(pageLinks, statusCode, content, url, contentLemmas, titleLemmas);
    }

    public String getHTMLTitle(String HTML) {
        return Jsoup.parse(HTML).select("title").text();
    }

    public String getHTMLSnippet(String HTML, List<Lemma> lemmas) {
        String HTMLText = Jsoup.parse(HTML).text();
        StringBuilder snippet = new StringBuilder();
        int offset = 35;

        String[] words = HTMLText.split(" ");

        Map<String, String> wordLemmaMap = Stream.of(HTMLText.split(" ")).collect(Collectors.toMap(
                s -> {
                    List<Lemma> lems = lemmatizer.getLemmas(s);
                    if (lems.size() > 0) {
                        return lems.get(0).getLemma();
                    }

                    return s;

                }
                , s -> s
                , (v1, v2) -> v1));

        for (Lemma lemma : lemmas) {
            String word = wordLemmaMap.get(lemma.getLemma());
            if (word != null) {
                int lemmaIndex = HTMLText.indexOf(word);
                if (lemmaIndex != -1) {
                    int startIndex = lemmaIndex - offset > 0 ? lemmaIndex - offset : lemmaIndex;
                    int finishIndex = lemmaIndex + offset < HTMLText.length() - 1 ? lemmaIndex + offset : lemmaIndex;

                    snippet.append(("..." + HTMLText.substring(startIndex, finishIndex) + "...").replace(word, "<b>" + word + "</b>"));
                }
            }
        }

        return snippet.toString();
    }
}
