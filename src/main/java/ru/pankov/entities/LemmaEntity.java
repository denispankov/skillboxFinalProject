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
    @Column(name = "lemma", nullable = false)
    String lemma;
    @Column(name = "frequency", nullable = false)
    int frequency;
    @ManyToOne(optional=false, cascade= CascadeType.ALL)
    @JoinColumn(name="site_id")
    private SiteEntity siteEntity;
}
