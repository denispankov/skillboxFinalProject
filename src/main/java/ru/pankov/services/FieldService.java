package ru.pankov.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.pankov.entities.FieldEntity;
import ru.pankov.repositories.FieldRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class FieldService {

    @Autowired
    private FieldRepository fieldRepository;

    @PostConstruct
    private void postConstruct(){
        List<FieldEntity> fieldEntityList = new ArrayList<>();
        fieldEntityList.add(new FieldEntity("title","title",1));
        fieldEntityList.add(new FieldEntity("body","body",0.8f));

        fieldEntityList.forEach(f -> {
            FieldEntity searchField = fieldRepository.findByName(f.getName());
            if (searchField == null) {
                fieldRepository.save(f);
            }
        });
    }

    public FieldEntity getFieldByName(String name){
        return fieldRepository.findByName(name);
    }
}
