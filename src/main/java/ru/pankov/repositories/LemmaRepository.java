package ru.pankov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.pankov.entities.LemmaEntity;
import ru.pankov.entities.SiteEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {
    List<LemmaEntity> findByLemmaInAndSiteEntity (List<String> lemma, SiteEntity siteEntity);

    LemmaEntity findByLemma(String lemma);

    @Transactional
    @Modifying
    @Query(value = "delete from lemma", nativeQuery = true)
    void deleteAllWithQuery();
}
