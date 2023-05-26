package ru.pankov.entities;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(schema = "public",name = "index")
@Data
public class IndexEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    Long id;

    @ManyToOne (optional=false, cascade=CascadeType.ALL)
    @JoinColumn (name="page_id")
    private PageEntity pageEntity;

    @ManyToOne (optional=false, cascade=CascadeType.ALL)
    @JoinColumn (name="lemma_id")
    private LemmaEntity lemmaEntity;

    @Column(name = "rank", nullable = false)
    float rank;
}
