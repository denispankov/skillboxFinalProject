package ru.pankov.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(schema = "public",name = "page")
@Data
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

    @OneToMany(mappedBy = "pageEntity")
    private Set<IndexEntity> indexEntitySet;
}
