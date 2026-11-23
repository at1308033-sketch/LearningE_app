package com.learninge.app.data.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.function.Supplier;

/**
 * Chạy nhiều thao tác ghi trong một giao dịch (NFR-A3). Lồng nhau được: giao dịch trong nhập vào giao dịch ngoài.
 */
public final class Tx {

    private Tx() {
    }

    public static <T> T run(DatabaseHelper database, Supplier<T> body) {
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
