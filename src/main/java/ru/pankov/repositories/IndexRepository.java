package ru.pankov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pankov.entities.IndexEntity;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {
}
