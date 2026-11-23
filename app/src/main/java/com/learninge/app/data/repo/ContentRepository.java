package com.learninge.app.data.repo;

import android.database.Cursor;

import com.learninge.app.data.db.ContentWriter;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.Tx;
import com.learninge.app.data.io.SeedContent.SeedDeck;
import com.learninge.app.data.io.SeedContent.SeedExam;
import com.learninge.app.data.io.SeedContent.SeedLanguage;

import java.time.Clock;
import java.util.List;

/**
 * Phiên bản nội dung đã cài và việc ghi nội dung mới trong một giao dịch (BR-075).
 */
public final class ContentRepository {
    private static final String GLOBAL = "global";

    private final DatabaseHelper database;
    private final Clock clock;

    public ContentRepository(DatabaseHelper database, Clock clock) {
        this.database = database;
        this.clock = clock;
    }

    public long installedVersion() {
        try (Cursor cursor = database.getReadableDatabase().rawQuery(
                "SELECT version FROM content_version WHERE item_key = ?", new String[]{GLOBAL})) {
            return cursor.moveToFirst() ? cursor.getLong(0) : 0;
        }
    }

    public boolean isNewer(String key, int version) {
        return new ContentWriter(database.getReadableDatabase(), clock).isNewer(key, version);
    }

    /**
     * Tất cả hoặc không gì cả: lỗi giữa chừng thì giữ nguyên nội dung cũ (AC-044.2).
     */
    public void install(long version, List<SeedLanguage> languages, List<SeedDeck> decks, List<SeedExam> exams) {
        Tx.run(database, () -> {
            ContentWriter writer = new ContentWriter(database.getWritableDatabase(), clock);
            languages.forEach(writer::putLanguage);
            decks.forEach(writer::putDeck);
            exams.forEach(writer::putExam);
            writer.installed(GLOBAL, version);
            return null;
        });
    }
}
