package ru.pankov.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(schema = "public",name = "lemma")
@Data
public class LemmaEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    Long id;
    @Column(name = "lemma", nullable = false, columnDefinition="text")
    String lemma;
    @Column(name = "frequency", nullable = false)
    double frequency;
    @ManyToOne(optional=false, cascade = {CascadeType.MERGE})
    @JoinColumn(name="site_id")
    private SiteEntity siteEntity;

    public LemmaEntity(){};

    public LemmaEntity(String lemma, double frequency, SiteEntity siteEntity) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteEntity = siteEntity;
    }

    @Override
    public boolean equals(Object o){
        return lemma == ((LemmaEntity)o).getLemma();
    }
}
