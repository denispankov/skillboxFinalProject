package ru.pankov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pankov.entities.PageEntity;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {
}
