package ru.pankov.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pankov.entities.LemmaEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {
    List<LemmaEntity> findByLemmaIn (List<String> lemma);

    LemmaEntity findByLemma(String lemma);
}
