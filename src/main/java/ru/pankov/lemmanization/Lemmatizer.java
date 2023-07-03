package ru.pankov.lemmanization;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.pankov.dto.lemmanization.Lemma;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private List<String> getRawLemmas(String text){
        List<String> filteredWords = new ArrayList<>();
        String textForLemmas = text.toLowerCase().replaceAll("[^А-я ]", "");
        if (!textForLemmas.equals("")) {
            try {
                Arrays.stream(textForLemmas.split(" ")).forEach(word -> {
                    if (!word.equals("")) {
                        List<String> lemmas = luceneMorphology.getMorphInfo(word);
                        if (!lemmas.get(0).matches(".*[СОЮЗ|МЕЖД|ПРЕДЛ|ЧАСТ]")) {
                            filteredWords.add(luceneMorphology.getNormalForms(word).get(0));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return filteredWords;
    }

    public List<Lemma> getLemmas(String text) {
        List<String> filteredWords = getRawLemmas(text);

        return filteredWords.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().map(e-> new Lemma(e.getKey())).collect(Collectors.toList());
    }

    public List<Lemma> getLemmasWithRank(String text, float multiplier) {
        List<String> filteredWords = getRawLemmas(text);

        return filteredWords.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream().map(e-> new Lemma(e.getKey(), e.getValue() * multiplier)).collect(Collectors.toList());

    }
}
