package ru.pankov.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(schema = "public",name = "things")
@Data
public class PageEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "path", nullable = false)
    private String path;
    @Column(name = "code", nullable = false)
    private int code;
    @Column(name = "text", nullable = false)
    private String text;

    @ManyToOne (optional=false, cascade=CascadeType.ALL)
    @JoinColumn (name="site_id")
    private SiteEntity siteEntity;
}
