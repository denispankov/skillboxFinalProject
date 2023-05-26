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
    int site_status;
    @Column(name = "status_time", nullable = false)
    LocalDateTime status_time;
    @Column(name = "last_error")
    String last_error;
    @Column(name = "url", nullable = false)
    String url;
    @Column(name = "name", nullable = false)
    String name;
}
