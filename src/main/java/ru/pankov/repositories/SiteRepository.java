package ru.pankov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pankov.dto.interfaces.SearchResultInterface;
import ru.pankov.dto.interfaces.SitesDetailStatisticInterface;
import ru.pankov.dto.interfaces.TotalStatisticInterface;
import ru.pankov.entities.SiteEntity;
import ru.pankov.enums.SiteStatus;

import java.util.List;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Long> {
    List<SiteEntity> findBySiteStatus(SiteStatus siteStatus);

    SiteEntity findByUrl(String url);

    @Transactional
    @Modifying
    @Query(value = "truncate table site cascade", nativeQuery = true)
    void deleteAllWithQuery();

    int countBySiteStatus(SiteStatus siteStatus);
}
