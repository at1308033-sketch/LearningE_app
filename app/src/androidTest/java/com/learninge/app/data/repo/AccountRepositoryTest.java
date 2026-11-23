package com.learninge.app.data.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.database.Cursor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.learninge.app.data.dao.SettingsDao;
import com.learninge.app.data.db.DatabaseHelper;
import com.learninge.app.data.model.User;
import com.learninge.app.data.remote.RemoteSession;
import com.learninge.app.domain.rules.NotebookId;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AccountRepositoryTest {
    private DatabaseHelper database;
    private AccountRepository accounts;

    @Before
    public void open() {
        database = new DatabaseHelper(InstrumentationRegistry.getInstrumentation().getTargetContext(), null);
        database.getWritableDatabase()
                .execSQL("INSERT INTO languages VALUES ('en', 'Tiếng Anh', 'en-US', 'Phiên âm (IPA)', 1)");
        accounts = new AccountRepository(database);
    }

    @After
    public void close() {
        database.close();
    }

    @Test
    public void signIn_createsDefaultsAndNotebookOnce() {
        User user = accounts.signIn(session("u-1", "a@learninge.test"), 10);
        User again = accounts.signIn(session("u-1", "a@learninge.test"), 20);
        assertEquals(user.id(), again.id());
        assertEquals(1, number("SELECT COUNT(*) FROM decks WHERE is_default = 1 AND uuid = '"
                + NotebookId.of("u-1") + "'"));
        assertEquals(10, new SettingsDao(database).find(user.id()).dailyGoal());
        assertEquals(1, number("SELECT COUNT(*) FROM sync_state"));
    }

    @Test
    public void signIn_sameAccount_keepsPendingData() {
        User user = accounts.signIn(session("u-1", "a@learninge.test"), 10);
        int pending = accounts.countPending(user.id());
        accounts.signIn(session("u-1", "new@learninge.test"), 20);
        assertEquals(pending, accounts.countPending(user.id()));
        assertEquals("new@learninge.test", accounts.findById(user.id()).email());
    }

    @Test
    public void signIn_otherAccount_removesOldData() {
        User old = accounts.signIn(session("u-1", "a@learninge.test"), 10);
        accounts.signIn(session("u-2", "b@learninge.test"), 20);
        assertNull(accounts.findById(old.id()));
        assertEquals(1, number("SELECT COUNT(*) FROM decks"));
    }

    private static RemoteSession session(String userId, String email) {
        return new RemoteSession("token", 0, userId, email, "minh", "Minh", 5);
    }

    private int number(String sql) {
        try (Cursor cursor = database.getReadableDatabase().rawQuery(sql, null)) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }
}
