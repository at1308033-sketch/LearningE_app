package com.learninge.app.data.repo;

import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.data.dao.DeckDao;
import com.learninge.app.data.dao.WordDao;
import com.learninge.app.data.db.DatabaseHelper;

import java.util.function.Supplier;

/**
 * Các thao tác ghi bộ từ cần nhiều câu lệnh trong một giao dịch.
 */
public final class DeckRepository {
    private final DatabaseHelper database;
    private final DeckDao decks;
    private final WordDao words;

    public DeckRepository(DatabaseHelper database) {
        this.database = database;
        decks = new DeckDao(database);
        words = new WordDao(database);
    }

    /**
     * Xóa bộ là đánh dấu xóa bộ và mọi từ (BR-045, BR-063); tiến độ của các từ đó tự hết hiệu lực.
     */
    public void delete(long deckId, long now) {
        inTransaction(() -> {
            words.markDeletedByDeck(deckId, now);
            decks.markDeleted(deckId, now);
            return null;
        });
    }

    /**
     * Kiểm tra và ghi trong cùng giao dịch để hai lần bấm không tạo bản trùng.
     */
    public <T> T inTransaction(Supplier<T> body) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            T result = body.get();
            db.setTransactionSuccessful();
            return result;
        } finally {
            db.endTransaction();
        }
    }
}
