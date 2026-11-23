package com.learninge.app.data.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Clock;

@RunWith(AndroidJUnit4.class)
public class SeedLoaderTest {
    private DatabaseHelper helper;
    private SQLiteDatabase db;
    private SeedLoader loader;

    @Before
    public void open() {
        helper = new DatabaseHelper(InstrumentationRegistry.getInstrumentation().getTargetContext(), null);
        db = helper.getWritableDatabase();
        // Seed mẫu của test: bộ T99 (2 từ) và đề E99 gắn với từ của T99.
        loader = new SeedLoader(InstrumentationRegistry.getInstrumentation().getContext(), db, Clock.systemUTC());
    }

    @After
    public void close() {
        helper.close();
    }

    @Test
    public void load_insertsContentNotDirty() {
        assertTrue(loader.loadIfNeeded("1").isOk());
        assertEquals(2, number("SELECT COUNT(*) FROM words WHERE dirty = 0"));
        assertEquals(1, number("SELECT COUNT(*) FROM decks WHERE code = 'T99' AND uuid = 'T99' AND dirty = 0"));
        assertEquals(1, number("SELECT COUNT(*) FROM exam_questions q JOIN words w ON w.id = q.word_id "
                + "WHERE w.term = 'apple'"));
    }

    @Test
    public void load_sameStamp_doesNothing() {
        loader.loadIfNeeded("1");
        db.execSQL("DELETE FROM words");
        loader.loadIfNeeded("1");
        assertEquals(0, number("SELECT COUNT(*) FROM words"));
    }

    @Test
    public void load_newApkWithSameVersion_keepsRows() {
        loader.loadIfNeeded("1");
        int id = number("SELECT id FROM words WHERE uuid = 'T99-0001'");
        db.execSQL("UPDATE words SET term = 'changed' WHERE uuid = 'T99-0001'");
        loader.loadIfNeeded("2");
        assertEquals(id, number("SELECT id FROM words WHERE term = 'changed'"));
    }

    @Test
    public void load_newerVersion_updatesInPlaceAndHidesRemovedWords() {
        loader.loadIfNeeded("1");
        int id = number("SELECT id FROM words WHERE uuid = 'T99-0001'");
        db.execSQL("UPDATE content_version SET version = 0");
        db.execSQL("UPDATE words SET term = 'changed', search_term = 'changed' WHERE uuid = 'T99-0001'");
        db.execSQL("INSERT INTO words (uuid, deck_id, term, meaning_vi, search_term, created_at, updated_at, dirty) "
                + "SELECT 'T99-0099', id, 'old', 'cũ', 'old', 0, 0, 0 FROM decks WHERE code = 'T99'");
        loader.loadIfNeeded("2");
        assertEquals(id, number("SELECT id FROM words WHERE term = 'apple'"));
        assertEquals(1, number("SELECT deleted FROM words WHERE uuid = 'T99-0099'"));
    }

    private int number(String sql) {
        try (Cursor cursor = db.rawQuery(sql, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : -1;
        }
    }
}
