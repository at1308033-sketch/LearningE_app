package com.learninge.app.domain.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.learninge.app.domain.rules.CsvCodec.FormatException;
import com.learninge.app.domain.rules.CsvCodec.Row;

import org.junit.Test;

import java.util.List;

public class CsvCodecTest {
    private static final String HEADER = "term,reading,meaning,example,example_translation\n";

    @Test
    public void parse_plainRowsKeepLineNumbers() throws Exception {
        List<Row> rows = CsvCodec.parse(HEADER + "cat,/kæt/,con mèo,,\n\ndog,,con chó,,");
        assertEquals(2, rows.size());
        assertEquals(2, rows.get(0).line());
        assertEquals(4, rows.get(1).line());
        assertEquals(List.of("dog", "", "con chó", "", ""), rows.get(1).cells());
    }

    @Test
    public void parse_quotesCommasAndNewlines() throws Exception {
        List<Row> rows = CsvCodec.parse(HEADER + "apple,,\"quả \"\"táo\"\"\",\"I eat, daily.\",\"dòng 1\ndòng 2\"\nx,,y,,");
        assertEquals("quả \"táo\"", rows.get(0).cells().get(2));
        assertEquals("I eat, daily.", rows.get(0).cells().get(3));
        assertEquals("dòng 1\ndòng 2", rows.get(0).cells().get(4));
        assertEquals(4, rows.get(1).line());
    }

    @Test
    public void parse_acceptsBomAndWindowsLineEnds() throws Exception {
        List<Row> rows = CsvCodec.parse("\uFEFF" + HEADER.replace("\n", "\r\n") + "cat,,mèo,,\r\n");
        assertEquals(List.of("cat", "", "mèo", "", ""), rows.get(0).cells());
    }

    @Test
    public void parse_wrongHeaderOrEmpty_fails() {
        assertThrows(FormatException.class, () -> CsvCodec.parse(""));
        assertThrows(FormatException.class, () -> CsvCodec.parse("từ,nghĩa\na,b"));
        assertThrows(FormatException.class, () -> CsvCodec.parse("reading,term,meaning,example,example_translation\n"));
    }

    @Test
    public void parse_unclosedQuote_fails() {
        FormatException error = assertThrows(FormatException.class, () -> CsvCodec.parse(HEADER + "a,,\"b,,\n"));
        assertTrue(error.getMessage().contains("dòng 2"));
    }

    @Test
    public void parse_headerOnly_hasNoRows() throws Exception {
        assertTrue(CsvCodec.parse(HEADER).isEmpty());
    }

    @Test
    public void format_roundTripsEveryCell() throws Exception {
        WordInput word = new WordInput("a,b", "/x/", "nghĩa \"1\"", "dòng 1\ndòng 2", null);
        String csv = CsvCodec.format(List.of(word));
        assertTrue(csv.startsWith("\uFEFFterm,reading"));
        List<String> cells = CsvCodec.parse(csv).get(0).cells();
        assertEquals(List.of("a,b", "/x/", "nghĩa \"1\"", "dòng 1\ndòng 2", ""), cells);
    }
}
