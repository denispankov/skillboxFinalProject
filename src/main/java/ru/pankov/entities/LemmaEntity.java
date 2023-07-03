package ru.pankov.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(schema = "public",name = "lemma",indexes = {@Index(name = "lemma_idx", columnList = "lemma, site_id",unique = true), @Index(name = "site_idx", columnList = "site_id")})
@Getter
@Setter
public class LemmaEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    Long id;
    @Column(name = "lemma", nullable = false, columnDefinition="text")
    String lemma;
    @Column(name = "frequency", nullable = false)
    int frequency;
    @ManyToOne(optional=false, cascade = {CascadeType.MERGE})
    @JoinColumn(name="site_id")
    private SiteEntity siteEntity;

    @ManyToMany
    @JoinTable(
            name = "index",
            joinColumns = @JoinColumn(name = "lemma_id"),
            inverseJoinColumns = @JoinColumn(name = "page_id")
    )
    private Set<PageEntity> pageEntitySet;



    public LemmaEntity(){};

    public LemmaEntity(String lemma, int frequency, SiteEntity siteEntity) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteEntity = siteEntity;
    }

    @Override
    public boolean equals(Object o){
        return lemma == ((LemmaEntity)o).getLemma();
    }
}
