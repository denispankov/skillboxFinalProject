package ru.pankov.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ru.pankov.enums.SiteStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(schema = "public",name = "site", indexes = {@Index(name = "site_url_idx", columnList = "url", unique = true)})
@Getter
@Setter
public class SiteEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    Long id;
    @Column(name = "site_status", nullable = false)
    @Enumerated(EnumType.STRING)
    SiteStatus siteStatus;
    @Column(name = "status_time", nullable = false)
    LocalDateTime statusTime;
    @Column(name = "last_error", columnDefinition="text")
    String lastError;
    @Column(name = "url", nullable = false, columnDefinition="text")
    String url;
    @Column(name = "name", nullable = false, columnDefinition="text")
    String name;

    @OneToMany(mappedBy = "siteEntity")
    Set<LemmaEntity> lemmaEntities;

    @OneToMany(mappedBy = "siteEntity")
    Set<PageEntity> pageEntities;
}
