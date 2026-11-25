package com.learninge.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.learninge.app.TestApp;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.remote.AuthRemote;
import com.learninge.app.data.repo.AccountRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AuthSessionsTest {
    private TestApp app;
    private ErrorCode error;
    private AuthService service;

    @Before
    public void open() {
        app = new TestApp();
        AuthRemote remote = new AuthRemote(null, "test") {
            @Override
            public Result<Void> revokeSession(long id) {
                return error == null ? Result.ok(null) : Result.error(error);
            }
        };
        service = new AuthService(() -> remote, new AccountRepository(app.services.database()),
                app.services.tokenStore(), app.services.sessionStore(), app.services.executors(), app.services.clock());
    }

    @After
    public void close() {
        app.close();
    }

    @Test
    public void revoke_alreadyGone_isSuccess() {
        error = ErrorCode.NOT_FOUND;
        assertTrue(service.revokeSession(9).isOk());
    }

    @Test
    public void revoke_otherErrors_areReported() {
        error = ErrorCode.NETWORK_OFFLINE;
        assertEquals(ErrorCode.NETWORK_OFFLINE, service.revokeSession(9).error);
    }
}
