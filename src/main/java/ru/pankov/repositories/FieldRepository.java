package ru.pankov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pankov.entities.FieldEntity;

@Repository
public interface FieldRepository extends JpaRepository<FieldEntity,Long> {
}
