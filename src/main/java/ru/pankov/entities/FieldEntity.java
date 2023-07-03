package ru.pankov.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(schema = "public",name = "field")
@Getter
@Setter
@NoArgsConstructor
public class FieldEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    Long id;
    @Column(name = "name", nullable = false, columnDefinition="text")
    String name;
    @Column(name = "selector", nullable = false, columnDefinition="text")
    String selector;
    @Column(name = "weight", nullable = false)
    float weight;

    public FieldEntity(String name, String selector, float weight) {
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }
}
