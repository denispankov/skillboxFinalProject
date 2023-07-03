package ru.pankov.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(schema = "public",name = "page", indexes = {@Index(name = "path_idx", columnList = "path, site_id", unique = true)})
@Getter
@Setter
@NoArgsConstructor
public class PageEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "path", nullable = false, columnDefinition="text")
    private String path;
    @Column(name = "code", nullable = false)
    private int code;
    @Column(name = "content", columnDefinition="text")
    private String content;

    @ManyToOne (optional=false, cascade = {CascadeType.MERGE})
    @JoinColumn (name="site_id")
    private SiteEntity siteEntity;

    @ManyToMany(mappedBy = "pageEntitySet")
    private Set<LemmaEntity> lemmaEntitySet;

    public PageEntity(String path, int code, String content, SiteEntity siteEntity) {
        this.path = path;
        this.code = code;
        this.content = content;
        this.siteEntity = siteEntity;
    }
}
