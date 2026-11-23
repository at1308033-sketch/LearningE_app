package com.learninge.app.data.dao;

import android.database.Cursor;

import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.db.DbContract.Languages;
import com.learninge.app.data.model.Language;

import java.util.ArrayList;
import java.util.List;

/**
 * Bảng {@code languages}.
 */
public final class LanguageDao {
    private static final String[] COLUMNS = {Languages.CODE, Languages.NAME_VI, Languages.TTS_LOCALE,
            Languages.READING_LABEL};

    private final DatabaseHelper database;

    public LanguageDao(DatabaseHelper database) {
        this.database = database;
    }

    public List<Language> listAll() {
        List<Language> result = new ArrayList<>();
        try (Cursor cursor = database.getReadableDatabase().query(Languages.TABLE, COLUMNS, null, null, null, null,
                Languages.SORT_ORDER)) {
            while (cursor.moveToNext()) {
                result.add(new Language(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3)));
            }
        }
        return result;
    }
}
