package ru.pankov.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(schema = "public",name = "site")
@Data
public class SiteEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    Long id;
    @Column(name = "site_status", nullable = false)
    int siteStatus;
    @Column(name = "status_time", nullable = false)
    LocalDateTime statusTime;
    @Column(name = "last_error", columnDefinition="text")
    String lastError;
    @Column(name = "url", nullable = false, columnDefinition="text")
    String url;
    @Column(name = "name", nullable = false, columnDefinition="text")
    String name;
}
