package com.learninge.app.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.db.DbContract.SeedMeta;
import com.learninge.app.data.io.SeedContent.SeedDeck;
import com.learninge.app.data.io.SeedContent.SeedExam;
import com.learninge.app.data.io.SeedContent.SeedLanguage;
import com.learninge.app.data.io.SeedParser;

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
    private final ContentWriter writer;

    public SeedLoader(Context context, SQLiteDatabase db, Clock clock) {
        this(context.getAssets(), db, clock);
    }

    SeedLoader(AssetManager assets, SQLiteDatabase db, Clock clock) {
        this.assets = assets;
        this.db = db;
        writer = new ContentWriter(db, clock);
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
            writer.putLanguage(language);
        }
        String[] decks = assets.list(ROOT + "/decks");
        for (String file : decks == null ? new String[0] : decks) {
            SeedDeck deck = SeedParser.deck(read(ROOT + "/decks/" + file));
            if (writer.isNewer("deck:" + deck.code(), deck.version())) {
                writer.putDeck(deck);
            }
        }
        if (files.contains("exams.json")) {
            for (SeedExam exam : SeedParser.exams(read(ROOT + "/exams.json"))) {
                if (writer.isNewer("exam:" + exam.code(), exam.version())) {
                    writer.putExam(exam);
                }
            }
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
