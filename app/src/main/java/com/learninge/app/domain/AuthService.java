package com.learninge.app.domain;

import android.database.SQLException;

import com.learninge.app.core.AppExecutors;
import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.core.SessionStore;
import com.learninge.app.data.model.DeviceSession;
import com.learninge.app.data.model.User;
import com.learninge.app.data.remote.AuthRemote;
import com.learninge.app.data.remote.RemoteSession;
import com.learninge.app.data.remote.TokenStore;
import com.learninge.app.data.repo.AccountRepository;
import com.learninge.app.domain.rules.AccountRules;
import com.learninge.app.domain.rules.RegisterForm;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Tài khoản qua máy chủ (FN-001, FN-002). Mọi phương thức chặn, gọi trên luồng {@code io}.
 */
public class AuthService {
    private final Supplier<AuthRemote> remote;
    private final AccountRepository accounts;
    private final TokenStore tokens;
    private final SessionStore session;
    private final AppExecutors executors;
    private final Clock clock;

    public AuthService(Supplier<AuthRemote> remote, AccountRepository accounts, TokenStore tokens,
                       SessionStore session, AppExecutors executors, Clock clock) {
        this.remote = remote;
        this.accounts = accounts;
        this.tokens = tokens;
        this.session = session;
        this.executors = executors;
        this.clock = clock;
    }

    /**
     * {@code detail} của lỗi VALIDATION là các trường sai, vd "EMAIL,PASSWORD".
     */
    public Result<User> register(RegisterForm form) {
        Set<AccountRules.Field> errors = AccountRules.checkRegister(form);
        if (!errors.isEmpty()) {
            return invalid(errors);
        }
        return complete(remote.get().register(form));
    }

    public Result<User> login(String email, String password) {
        Set<AccountRules.Field> errors = AccountRules.checkLogin(email, password);
        if (!errors.isEmpty()) {
            return invalid(errors);
        }
        return complete(remote.get().login(email, password));
    }

    /**
     * Thu hồi token và xóa dữ liệu người dùng trên máy (BR-073). Nơi gọi phải đẩy hết hoặc hỏi người dùng trước.
     */
    public Result<Void> logout() {
        long userId = session.userId();
        // Không gọi được EP-04 thì vẫn đăng xuất: token trên máy bị xóa, token trên server tự hết hạn (D-01).
        remote.get().logout();
        tokens.clear();
        session.signOut();
        return write(() -> {
            accounts.delete(userId);
            return null;
        });
    }

    /**
     * Các máy đang đăng nhập tài khoản này, dùng gần nhất trước.
     */
    public Result<List<DeviceSession>> sessions() {
        return remote.get().sessions();
    }

    /**
     * Đăng xuất một máy khác. Phiên đã hết trước đó cũng là thành công.
     */
    public Result<Void> revokeSession(long id) {
        Result<Void> result = remote.get().revokeSession(id);
        return result.error == ErrorCode.NOT_FOUND ? Result.ok(null) : result;
    }

    public Result<Void> revokeOtherSessions() {
        return remote.get().revokeOtherSessions();
    }

    public Result<User> currentUser() {
        User user = session.isSignedIn() ? accounts.findById(session.userId()) : null;
        return user == null ? Result.error(ErrorCode.NOT_FOUND) : Result.ok(user);
    }

    /**
     * Email của tài khoản còn dữ liệu trên máy, để điền sẵn ở màn đăng nhập; null nếu không có.
     */
    public String lastEmail() {
        User last = accounts.findLast();
        return last == null ? null : last.email();
    }

    /**
     * Số thay đổi chưa đồng bộ sẽ mất nếu đăng nhập bằng tài khoản khác với tài khoản còn dữ liệu trên máy.
     */
    public int pendingLostBySwitchingTo(String email) {
        User last = accounts.findLast();
        boolean isOther = last != null && !last.email().equals(AccountRules.normalizeEmail(email));
        return isOther ? accounts.countPending(last.id()) : 0;
    }

    /**
     * Máy chủ từ chối phiên (BR-059): hết hạn thì giữ dữ liệu, tài khoản đã xóa thì xóa luôn dữ liệu trên máy.
     */
    public void endSession(ErrorCode reason) {
        long userId = session.userId();
        tokens.clear();
        session.signOut();
        if (reason == ErrorCode.ACCOUNT_DELETED) {
            write(() -> {
                accounts.delete(userId);
                return null;
            });
        }
    }

    private Result<User> complete(Result<RemoteSession> result) {
        if (!result.isOk()) {
            return Result.errorOf(result);
        }
        Result<User> saved = write(() -> accounts.signIn(result.value, clock.millis()));
        if (saved.isOk()) {
            tokens.save(result.value.token(), result.value.expiresAtMillis());
            session.signIn(saved.value.id());
        }
        return saved;
    }

    private <T> Result<T> write(Supplier<T> task) {
        try {
            return Result.ok(executors.write(task));
        } catch (SQLException failed) {
            return Result.error(ErrorCode.DB, failed.getMessage());
        }
    }

    private static Result<User> invalid(Set<AccountRules.Field> errors) {
        return Result.error(ErrorCode.VALIDATION, errors.stream().map(Enum::name).collect(Collectors.joining(",")));
    }
}
