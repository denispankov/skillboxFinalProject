package ru.pankov.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.entities.IndexEntity;
import ru.pankov.entities.PageEntity;
import ru.pankov.entities.SiteEntity;
import ru.pankov.repositories.IndexRepository;
import ru.pankov.services.interfaces.DbCleaner;

import java.util.List;

@Service
public class IndexService implements DbCleaner {
    
    @Autowired
    private IndexRepository indexRepository;
    
    public void deleteIndex(PageEntity pageEntity){
        List<IndexEntity> indexEntityList = indexRepository.findByPageEntity(pageEntity);
        indexRepository.deleteAll(indexEntityList);
    }

    public void deleteAll(){
        indexRepository.deleteAllWithQuery();
    }
}
