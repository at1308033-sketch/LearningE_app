package com.learninge.app.domain.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.List;
import java.util.Set;

public class ImportPreviewTest {

    @Test
    public void of_sortsRowsIntoValidDuplicateAndError() throws Exception {
        ImportPreview preview = ImportPreview.of("tu_vung.csv", CsvCodec.parse("term,reading,meaning,example,"
                + "example_translation\ncat,,mèo,,\n Cat ,,mèo nữa,,\ndog,,,,\na,b\n" + "x".repeat(51) + ",,y,,\n"));
        assertEquals("tu_vung", preview.deckName());
        assertEquals(5, preview.total());
        assertEquals(1, preview.words().size());
        assertEquals(1, preview.duplicates());
        assertEquals(3, preview.errors().size());
        assertEquals(DeckRules.Field.MEANING, preview.errors().get(0).reason());
        assertEquals(4, preview.errors().get(0).line());
        assertNull(preview.errors().get(1).reason());
        assertEquals(DeckRules.Field.TERM, preview.errors().get(2).reason());
    }

    @Test
    public void of_fileNameEdgeCases() {
        assertEquals("a.b", ImportPreview.of("a.b.csv", List.of()).deckName());
        assertEquals("Bộ nhập từ tệp", ImportPreview.of("", List.of()).deckName());
        assertEquals(50, ImportPreview.of("x".repeat(80) + ".csv", List.of()).deckName().length());
    }

    @Test
    public void freeName_addsNumberWithinLimit() {
        assertEquals("a (3)", ImportPreview.freeName("a", Set.of("a", "a (2)")::contains));
        String longName = "x".repeat(50);
        assertEquals("x".repeat(46) + " (2)", ImportPreview.freeName(longName, longName::equals));
    }

    @Test
    public void sample_isAtMostFive() throws Exception {
        StringBuilder csv = new StringBuilder("term,reading,meaning,example,example_translation\n");
        for (int i = 0; i < 7; i++) {
            csv.append("w").append(i).append(",,n,,\n");
        }
        assertEquals(5, ImportPreview.of("f", CsvCodec.parse(csv.toString())).sample().size());
    }
}
