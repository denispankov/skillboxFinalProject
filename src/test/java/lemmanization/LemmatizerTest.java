package lemmanization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.pankov.dto.lemmanization.Lemma;
import ru.pankov.lemmanization.Lemmatizer;

import java.util.List;

@SpringBootTest(classes = {Lemmatizer.class})
public class LemmatizerTest {

    @Autowired
    private Lemmatizer lemmatizer;

    @Test
    @DisplayName("Invalid parts of speech")
    public void testInvalidPartsOfSpeech(){
        String invalidPartsOfSpeech = "чтобы, ах, до, разве";

        List<Lemma> lemmaList = lemmatizer.getLemmas(invalidPartsOfSpeech);

        Assertions.assertEquals(0,lemmaList.size());
    }

    @Test
    @DisplayName("One part of speech")
    public void testOnePartsOfSpeech(){
        String partOfSpeech = "тестовый";

        List<Lemma> lemmaList = lemmatizer.getLemmas(partOfSpeech);

        Assertions.assertEquals(1,lemmaList.size());
    }

    @Test
    @DisplayName("Many part of speech")
    public void testManyPartsOfSpeech(){
        String partsOfSpeech = "тестовый, теструю, тесты";

        List<Lemma> lemmaList = lemmatizer.getLemmas(partsOfSpeech);

        Assertions.assertEquals(3,lemmaList.size());
    }

    @Test
    @DisplayName("Test rank")
    public void testLemmaRank(){
        String partOfSpeech = "тестовый";

        List<Lemma> lemmaList = lemmatizer.getLemmasWithRank(partOfSpeech, 0.5f);

        Assertions.assertEquals(0.5f,lemmaList.get(0).getRank());
    }
}
