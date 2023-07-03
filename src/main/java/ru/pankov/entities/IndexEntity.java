package ru.pankov.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "public",name = "index",indexes = {@Index(name = "index_lemma_fk_idx", columnList = "lemma_id"), @Index(name = "index_page_fk_idx", columnList = "page_id")})
@Getter
@Setter
public class IndexEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    Long id;

    @ManyToOne (optional=false, cascade = {CascadeType.MERGE})
    @JoinColumn (name="page_id")
    private PageEntity pageEntity;

    @ManyToOne (optional=false, cascade = {CascadeType.MERGE})
    @JoinColumn (name="lemma_id")
    private LemmaEntity lemmaEntity;

    @Column(name = "rank", nullable = false)
    double rank;

    public IndexEntity(PageEntity pageEntity, LemmaEntity lemmaEntity, double rank) {
        this.pageEntity = pageEntity;
        this.lemmaEntity = lemmaEntity;
        this.rank = rank;
    }

    public IndexEntity(){

    }
}
