package ru.pankov.lemmanization;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class Lemmatizer {
    private LuceneMorphology luceneMorphology;

    public Lemmatizer() {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Lemma> getLemmas(String text) {
        List<String> filteredWords = new ArrayList<>();
        String textForLemmas = text.toLowerCase().replaceAll("[^А-я ]", "");
        if (textForLemmas != "") {
            try {
                Arrays.stream(textForLemmas.split(" ")).forEach(word -> {
                    if (word != "") {
                        List<String> lemmas = luceneMorphology.getMorphInfo(word);
                        if (!lemmas.get(0).matches(".*[СОЮЗ|МЕЖД|ПРЕДЛ]")) {
                            filteredWords.add(luceneMorphology.getNormalForms(word).get(0));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return filteredWords.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().map(e-> new Lemma(e.getKey(), e.getValue())).collect(Collectors.toList());
    }
}
