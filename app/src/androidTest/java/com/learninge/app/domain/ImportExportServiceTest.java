package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.learninge.app.TestApp;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.domain.ImportExportService.ImportResult;
import com.learninge.app.domain.rules.ImportPreview;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RunWith(AndroidJUnit4.class)
public class ImportExportServiceTest {
    private static final String HEADER = "term,reading,meaning,example,example_translation\n";

    private TestApp app;
    private ImportExportService service;

    @Before
    public void open() {
        app = new TestApp();
        service = app.services.importExportService();
        app.deck(1, "T01", null, "A1");
        app.word(11, 1, "mother");
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void importInto_newDeck_usesFileNameOnce() {
        ImportPreview preview = service.preview("Công việc.csv", HEADER + "cat,,mèo,,\ndog,,chó,,\n").value;
        ImportResult first = service.importInto(preview, ImportExportService.NEW_DECK).value;
        ImportResult second = service.importInto(preview, ImportExportService.NEW_DECK).value;
        assertEquals(2, first.imported());
        assertEquals("Công việc (2)", app.text("SELECT name FROM decks WHERE id = " + second.deckId()));
        assertEquals(2, app.number("SELECT COUNT(*) FROM words WHERE dirty = 1 AND deck_id = " + first.deckId()));
    }

    @Test
    public void importInto_existingDeck_skipsWordsAlreadyThere() {
        ImportPreview preview = service.preview("f.csv", HEADER + "cat,,mèo,,\ndog,,chó,,\nbad,,,,\n").value;
        long deckId = service.importInto(preview, ImportExportService.NEW_DECK).value.deckId();
        ImportResult again = service.importInto(preview, deckId).value;
        assertEquals(0, again.imported());
        assertEquals(3, again.skipped());
    }

    @Test
    public void importInto_builtinDeckOrNothingValid_changesNothing() {
        ImportPreview preview = service.preview("f.csv", HEADER + "cat,,mèo,,\n").value;
        assertEquals(ErrorCode.READ_ONLY, service.importInto(preview, 1).error);
        ImportPreview empty = service.preview("f.csv", HEADER).value;
        assertEquals(ErrorCode.NO_VALID_ROWS, service.importInto(empty, ImportExportService.NEW_DECK).error);
        assertEquals(1, app.number("SELECT COUNT(*) FROM decks"));
    }

    @Test
    public void importInto_2000Rows_isFast() {
        StringBuilder csv = new StringBuilder(HEADER);
        for (int i = 0; i < 2000; i++) {
            csv.append("word").append(i).append(",,nghĩa,,\n");
        }
        ImportPreview preview = service.preview("f.csv", csv.toString()).value;
        long start = System.currentTimeMillis();
        assertEquals(2000, service.importInto(preview, ImportExportService.NEW_DECK).value.imported());
        assertTrue(System.currentTimeMillis() - start < 3_000);
        csv.append("extra,,x,,\n");
        assertEquals(ErrorCode.FILE_TOO_LARGE, service.preview("f.csv", csv.toString()).error);
    }

    @Test
    public void preview_wrongHeaderOrEncoding_isFormatError() throws Exception {
        assertEquals(ErrorCode.FILE_FORMAT, service.preview("f.csv", "a,b\n").error);
        File file = file("bad.csv");
        Files.write(file.toPath(), new byte[]{(byte) 0xC3, (byte) 0x28});
        assertEquals(ErrorCode.FILE_FORMAT, service.preview(Uri.fromFile(file)).error);
        assertEquals(ErrorCode.IO, service.preview(Uri.fromFile(file("missing.csv"))).error);
    }

    @Test
    public void export_thenImport_givesSameWords() throws Exception {
        File file = file("out.csv");
        assertEquals(1, (int) service.export(1, Uri.fromFile(file)).value);
        String csv = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFF"));
        ImportPreview preview = service.preview(Uri.fromFile(file)).value;
        assertEquals("mother", preview.words().get(0).term());
        assertEquals("nghĩa mother", preview.words().get(0).meaning());
    }

    @Test
    public void exportName_hasDateAndNoForbiddenCharacters() {
        assertEquals("A_B_20261110.csv", service.exportName("A/B"));
        assertFalse(service.exportName("x:y").contains(":"));
    }

    private static File file(String name) {
        File file = new File(InstrumentationRegistry.getInstrumentation().getTargetContext().getCacheDir(), name);
        file.delete();
        return file;
    }
}
