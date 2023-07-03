package ru.pankov.repositories;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pankov.entities.IndexEntity;
import ru.pankov.entities.LemmaEntity;
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

    List<IndexEntity> findByPageEntityInAndLemmaEntityIn(List<PageEntity> pageEntity, List<LemmaEntity> lemmaEntity);

    @Query(value = "select sum(i.rank) " +
            "from lemma l " +
            "join index i on i.lemma_id = l.id and i.page_id = :pageId " +
            "where l.lemma in (:lemmasList)", nativeQuery = true)
    double sumRankByPageIdAndLemmaNameIn(@Param("pageId") Long PageId, @Param("lemmasList") List<String> lemmaList);
}
