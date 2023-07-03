package ru.pankov.dto.lemmanization;

import lombok.Data;

@Data
public class Lemma{
    private String lemma;
    private double rank;
    private int frequency;

    public Lemma(String lemma, double rank) {
        this.lemma = lemma;
        this.rank = rank;
    }

    public Lemma(String lemma, int frequency) {
        this.lemma = lemma;
        this.frequency = frequency;
    }

    public Lemma(String lemma){
        this.lemma = lemma;
    }

    @Override
    public boolean equals(Object o){
        return lemma == ((Lemma)o).getLemma();
    }
}
