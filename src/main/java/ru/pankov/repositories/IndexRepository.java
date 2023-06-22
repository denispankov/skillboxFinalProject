package ru.pankov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pankov.entities.IndexEntity;
import ru.pankov.entities.PageEntity;
import ru.pankov.entities.SiteEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {
    @Transactional
    @Modifying
    @Query(value = "truncate table \"index\" cascade", nativeQuery = true)
    void deleteAllWithQuery();

    List<IndexEntity> findByPageEntity(PageEntity pageEntity);
}
