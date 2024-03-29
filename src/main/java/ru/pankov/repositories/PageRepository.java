package ru.pankov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pankov.entities.PageEntity;
import ru.pankov.entities.SiteEntity;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {

    @Transactional
    @Modifying
    @Query(value = "truncate table page cascade", nativeQuery = true)
    void deleteAllWithQuery();

    PageEntity findByPath(String path);

    int countBySiteEntity(SiteEntity siteEntity);
}
