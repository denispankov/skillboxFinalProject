package ru.pankov.lemmanization;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Lemmatizer {
    private  LuceneMorphology luceneMorphology;

    public Lemmatizer(){
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public Map<String, Long> getLemmas(String text){
        List<String> filteredWords = new ArrayList<>();
        try {
        Arrays.stream(text.replaceAll("[.|,|:|;|\n]", "").split(" ")).forEach(word -> {
            List<String> test = luceneMorphology.getMorphInfo(word);
            if (!luceneMorphology.getMorphInfo(word).get(0).matches(".*[СОЮЗ|МЕЖД|ПРЕДЛ]")) {
                filteredWords.add(luceneMorphology.getNormalForms(word).get(0));
            }
        });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filteredWords.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }
}
