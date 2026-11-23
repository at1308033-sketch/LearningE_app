package com.learninge.app.data.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import com.learninge.app.data.io.SeedContent.SeedDeck;
import com.learninge.app.data.io.SeedContent.SeedExam;

import org.json.JSONException;
import org.junit.Test;

public class SeedParserTest {
    private static final String DECK = "{\"deck\":{\"code\":\"T01\",\"version\":2,\"name\":\"Gia đình\",\"language\":"
            + "\"en\",\"icon\":\"t01-family\",\"level\":\"A1\",\"exam_tag\":null,\"sort_order\":1},\"source\":\"Tự soạn\","
            + "\"words\":[{\"uid\":\"T01-0001\",\"term\":\"mother\",\"meaning\":\"mẹ\"}]}";

    @Test
    public void deck_readsRequiredAndOptionalFields() throws Exception {
        SeedDeck deck = SeedParser.deck(DECK);
        assertEquals(2, deck.version());
        assertNull(deck.examTag());
        assertEquals("mẹ", deck.words().get(0).meaning());
        assertNull(deck.words().get(0).reading());
    }

    @Test
    public void deck_missingMeaning_fails() {
        assertThrows(JSONException.class, () -> SeedParser.deck(DECK.replace("\"meaning\"", "\"nghia\"")));
    }

    @Test
    public void exam_readsQuestions() throws Exception {
        SeedExam exam = SeedParser.exams(exam("[\"a\",\"b\",\"c\",\"d\"]")).get(0);
        assertEquals(900, exam.durationSec());
        assertEquals(1, exam.questions().size());
        assertEquals("submit", exam.questions().get(0).term());
    }

    @Test
    public void exam_needsExactlyFourOptions() {
        assertThrows(JSONException.class, () -> SeedParser.exams(exam("[\"a\",\"b\",\"c\"]")));
    }

    private static String exam(String options) {
        return "{\"tests\":[{\"code\":\"E01\",\"exam_tag\":\"TOEIC\",\"title\":\"Đề 1\",\"duration_sec\":900,"
                + "\"questions\":[{\"number\":1,\"question\":\"?\",\"options\":" + options + ",\"correct_index\":1,"
                + "\"deck_code\":\"X01\",\"term\":\"submit\"}]}]}";
    }
}
