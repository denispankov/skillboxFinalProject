package ru.pankov.pojo.lemmanization;

import lombok.Data;

@Data
public class Lemma{
    private String lemma;
    private Long count;
    private double rank;

    public Lemma(String lemma, Long count) {
        this.lemma = lemma;
        this.count = count;
    }

    public Lemma(String lemma, double rank) {
        this.lemma = lemma;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o){
        return lemma == ((Lemma)o).getLemma();
    }
}
