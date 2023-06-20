package services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.pankov.dto.lemmanization.Lemma;
import ru.pankov.services.lemmanization.LemmatizerService;

import java.util.List;

@SpringBootTest(classes = {LemmatizerService.class})
public class LemmatizerServiceTest {

    @Autowired
    private LemmatizerService lemmatizerService;

    @Test
    @DisplayName("Invalid parts of speech")
    public void testInvalidPartsOfSpeech(){
        String invalidPartsOfSpeech = "чтобы, ах, до, разве";

        List<Lemma> lemmaList = lemmatizerService.getLemmas(invalidPartsOfSpeech);

        Assertions.assertEquals(0,lemmaList.size());
    }

    @Test
    @DisplayName("One part of speech")
    public void testOnePartsOfSpeech(){
        String partOfSpeech = "тестовый";

        List<Lemma> lemmaList = lemmatizerService.getLemmas(partOfSpeech);

        Assertions.assertEquals(1,lemmaList.size());
    }

    @Test
    @DisplayName("Many part of speech")
    public void testManyPartsOfSpeech(){
        String partsOfSpeech = "тестовый, теструю, тесты";

        List<Lemma> lemmaList = lemmatizerService.getLemmas(partsOfSpeech);

        Assertions.assertEquals(3,lemmaList.size());
    }
}
