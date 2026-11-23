package com.learninge.app.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.db.DbContract.ContentVersion;
import com.learninge.app.data.db.DbContract.Decks;
import com.learninge.app.data.db.DbContract.ExamQuestions;
import com.learninge.app.data.db.DbContract.ExamTests;
import com.learninge.app.data.db.DbContract.Languages;
import com.learninge.app.data.db.DbContract.SeedMeta;
import com.learninge.app.data.db.DbContract.Sync;
import com.learninge.app.data.db.DbContract.Words;
import com.learninge.app.data.io.SeedContent.SeedDeck;
import com.learninge.app.data.io.SeedContent.SeedExam;
import com.learninge.app.data.io.SeedContent.SeedLanguage;
import com.learninge.app.data.io.SeedContent.SeedQuestion;
import com.learninge.app.data.io.SeedContent.SeedWord;
import com.learninge.app.data.io.SeedParser;
import com.learninge.app.domain.rules.TextNormalizer;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;

/**
 * Nạp nội dung có sẵn từ {@code assets/seed} vào CSDL trong một giao dịch. Chạy lại bao nhiêu lần cũng được: chỉ mục có
 * {@code version} lớn hơn bản đã cài mới được ghi, và từ được sửa tại chỗ theo {@code uid} nên tiến độ học không mất.
 */
public final class SeedLoader {
    private static final String ROOT = "seed";
    private static final String META_STAMP = "seed_stamp";

    private final AssetManager assets;
    private final SQLiteDatabase db;
    private final Clock clock;

    public SeedLoader(Context context, SQLiteDatabase db, Clock clock) {
        this(context.getAssets(), db, clock);
    }

    SeedLoader(AssetManager assets, SQLiteDatabase db, Clock clock) {
        this.assets = assets;
        this.db = db;
        this.clock = clock;
    }

    /**
     * @param stamp đổi mỗi khi APK được cài lại; giống lần trước thì không cần đọc file.
     */
    public Result<Void> loadIfNeeded(String stamp) {
        if (stamp.equals(meta(META_STAMP))) {
            return Result.ok(null);
        }
        db.beginTransaction();
        try {
            loadAll();
            putMeta(META_STAMP, stamp);
            db.setTransactionSuccessful();
            return Result.ok(null);
        } catch (JSONException | IOException broken) {
            return Result.error(ErrorCode.FILE_FORMAT, broken.getMessage());
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    private void loadAll() throws JSONException, IOException {
        List<String> files = List.of(assets.list(ROOT));
        for (SeedLanguage language : SeedParser.languages(read(ROOT + "/languages.json"))) {
            putLanguage(language);
        }
        String[] decks = assets.list(ROOT + "/decks");
        for (String file : decks == null ? new String[0] : decks) {
            SeedDeck deck = SeedParser.deck(read(ROOT + "/decks/" + file));
            if (shouldInstall("deck:" + deck.code(), deck.version())) {
                putDeck(deck);
            }
        }
        if (files.contains("exams.json")) {
            for (SeedExam exam : SeedParser.exams(read(ROOT + "/exams.json"))) {
                if (shouldInstall("exam:" + exam.code(), exam.version())) {
                    putExam(exam);
                }
            }
        }
    }

    private void putLanguage(SeedLanguage language) {
        ContentValues values = new ContentValues();
        values.put(Languages.CODE, language.code());
        values.put(Languages.NAME_VI, language.nameVi());
        values.put(Languages.TTS_LOCALE, language.ttsLocale());
        values.put(Languages.READING_LABEL, language.readingLabel());
        values.put(Languages.SORT_ORDER, language.sortOrder());
        updateOrInsert(Languages.TABLE, values, Languages.CODE, language.code());
    }

    private void putDeck(SeedDeck deck) {
        long now = clock.millis();
        ContentValues values = new ContentValues();
        values.put(Sync.UUID, deck.code());
        values.put(Decks.CODE, deck.code());
        values.put(Decks.NAME, deck.name());
        values.put(Decks.NAME_NORM, TextNormalizer.normalize(deck.name()));
        values.put(Decks.DESCRIPTION, deck.description());
        values.put(Decks.LANGUAGE_CODE, deck.language());
        values.put(Decks.LEVEL, deck.level());
        values.put(Decks.ICON, deck.icon());
        values.put(Decks.EXAM_TAG, deck.examTag());
        values.put(Decks.SORT_ORDER, deck.sortOrder());
        values.put(Decks.CREATED_AT, now);
        values.put(Sync.UPDATED_AT, now);
        values.put(Sync.DELETED, 0);
        values.put(Sync.DIRTY, 0);
        long deckId = updateOrInsert(Decks.TABLE, values, Sync.UUID, deck.code());

        // Từ bị bỏ khỏi file thì chỉ đánh dấu xóa vì tiến độ học còn trỏ tới.
        ContentValues hidden = new ContentValues();
        hidden.put(Sync.DELETED, 1);
        db.update(Words.TABLE, hidden, Words.DECK_ID + " = ?", new String[]{String.valueOf(deckId)});
        int order = 0;
        for (SeedWord word : deck.words()) {
            putWord(deckId, deck.language(), ++order, word, now);
        }
        installed("deck:" + deck.code(), deck.version());
    }

    private void putWord(long deckId, String language, int order, SeedWord word, long now) {
        ContentValues values = new ContentValues();
        values.put(Sync.UUID, word.uid());
        values.put(Words.DECK_ID, deckId);
        values.put(Words.TERM, word.term());
        values.put(Words.READING, word.reading());
        values.put(Words.PART_OF_SPEECH, word.partOfSpeech());
        values.put(Words.MEANING_VI, word.meaning());
        values.put(Words.EXAMPLE, word.example());
        values.put(Words.EXAMPLE_TRANSLATION, word.exampleTranslation());
        values.put(Words.SEARCH_TERM, TextNormalizer.searchKey(word.term(), language));
        values.put(Words.SEARCH_READING, word.reading() == null ? null : TextNormalizer.stripMarks(word.reading()));
        values.put(Words.SEARCH_MEANING, TextNormalizer.stripMarks(word.meaning()));
        values.put(Words.SORT_ORDER, order);
        values.put(Words.CREATED_AT, now);
        values.put(Sync.UPDATED_AT, now);
        values.put(Sync.DELETED, 0);
        values.put(Sync.DIRTY, 0);
        updateOrInsert(Words.TABLE, values, Sync.UUID, word.uid());
    }

    private void putExam(SeedExam exam) {
        ContentValues values = new ContentValues();
        values.put(ExamTests.CODE, exam.code());
        values.put(ExamTests.EXAM_TAG, exam.examTag());
        values.put(ExamTests.TITLE, exam.title());
        values.put(ExamTests.PART_LABEL, exam.partLabel());
        values.put(ExamTests.QUESTION_COUNT, exam.questions().size());
        values.put(ExamTests.DURATION_SEC, exam.durationSec());
        values.put(ExamTests.DESCRIPTION, exam.description());
        values.put(ExamTests.SORT_ORDER, exam.sortOrder());
        long testId = updateOrInsert(ExamTests.TABLE, values, ExamTests.CODE, exam.code());
        for (SeedQuestion question : exam.questions()) {
            putQuestion(testId, question);
        }
        db.delete(ExamQuestions.TABLE, ExamQuestions.TEST_ID + " = ? AND " + ExamQuestions.NUMBER + " > ?",
                new String[]{String.valueOf(testId), String.valueOf(exam.questions().size())});
        installed("exam:" + exam.code(), exam.version());
    }

    private void putQuestion(long testId, SeedQuestion question) {
        ContentValues values = new ContentValues();
        values.put(ExamQuestions.TEST_ID, testId);
        values.put(ExamQuestions.NUMBER, question.number());
        values.put(ExamQuestions.PASSAGE, question.passage());
        values.put(ExamQuestions.QUESTION, question.question());
        values.put(ExamQuestions.OPTIONS_JSON, question.optionsJson());
        values.put(ExamQuestions.CORRECT_INDEX, question.correctIndex());
        values.put(ExamQuestions.EXPLANATION, question.explanation());
        values.put(ExamQuestions.WORD_ID, wordId(question.deckCode(), question.term()));
        String[] key = {String.valueOf(testId), String.valueOf(question.number())};
        int updated = db.update(ExamQuestions.TABLE, values,
                ExamQuestions.TEST_ID + " = ? AND " + ExamQuestions.NUMBER + " = ?", key);
        if (updated == 0) {
            db.insertOrThrow(ExamQuestions.TABLE, null, values);
        }
    }

    private Long wordId(String deckCode, String term) {
        if (deckCode == null || term == null) {
            return null;
        }
        try (Cursor cursor = db.rawQuery("SELECT w.id FROM words w JOIN decks d ON d.id = w.deck_id "
                        + "WHERE d.code = ? AND w.search_term = ? AND w.deleted = 0",
                new String[]{deckCode, TextNormalizer.stripMarks(term)})) {
            return cursor.moveToFirst() ? Long.valueOf(cursor.getLong(0)) : null;
        }
    }

    private boolean shouldInstall(String key, int version) {
        try (Cursor cursor = db.query(ContentVersion.TABLE, new String[]{ContentVersion.VERSION},
                ContentVersion.ITEM_KEY + " = ?", new String[]{key}, null, null, null)) {
            return !cursor.moveToFirst() || cursor.getInt(0) < version;
        }
    }

    private void installed(String key, int version) {
        ContentValues values = new ContentValues();
        values.put(ContentVersion.ITEM_KEY, key);
        values.put(ContentVersion.VERSION, version);
        values.put(ContentVersion.INSTALLED_AT, clock.millis());
        db.replaceOrThrow(ContentVersion.TABLE, null, values);
    }

    // Không UPSERT (SQLite của Android 10): UPDATE, 0 dòng thì INSERT; giữ nguyên id để khóa ngoại không đứt.
    private long updateOrInsert(String table, ContentValues values, String keyColumn, String key) {
        String[] args = {key};
        if (db.update(table, values, keyColumn + " = ?", args) == 0) {
            return db.insertOrThrow(table, null, values);
        }
        try (Cursor cursor = db.query(table, new String[]{"rowid"}, keyColumn + " = ?", args, null, null, null)) {
            cursor.moveToFirst();
            return cursor.getLong(0);
        }
    }

    private String meta(String key) {
        try (Cursor cursor = db.query(SeedMeta.TABLE, new String[]{SeedMeta.VALUE}, SeedMeta.KEY + " = ?",
                new String[]{key}, null, null, null)) {
            return cursor.moveToFirst() ? cursor.getString(0) : null;
        }
    }

    private void putMeta(String key, String value) {
        ContentValues values = new ContentValues();
        values.put(SeedMeta.KEY, key);
        values.put(SeedMeta.VALUE, value);
        db.replaceOrThrow(SeedMeta.TABLE, null, values);
    }

    private String read(String path) throws IOException {
        try (InputStream in = assets.open(path); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            for (int n = in.read(buffer); n > 0; n = in.read(buffer)) {
                out.write(buffer, 0, n);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}
