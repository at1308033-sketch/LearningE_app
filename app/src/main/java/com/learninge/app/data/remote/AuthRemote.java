package com.learninge.app.data.remote;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.data.model.DeviceSession;
import com.learninge.app.domain.rules.AccountRules;
import com.learninge.app.domain.rules.RegisterForm;
import com.learninge.proto.app.v1.AuthServiceGrpc;
import com.learninge.proto.app.v1.AuthSession;
import com.learninge.proto.app.v1.ChangePasswordRequest;
import com.learninge.proto.app.v1.ChangePasswordResponse;
import com.learninge.proto.app.v1.GetResetQuestionRequest;
import com.learninge.proto.app.v1.GetResetQuestionResponse;
import com.learninge.proto.app.v1.ListSessionsRequest;
import com.learninge.proto.app.v1.ListSessionsResponse;
import com.learninge.proto.app.v1.LoginRequest;
import com.learninge.proto.app.v1.LogoutRequest;
import com.learninge.proto.app.v1.RegisterRequest;
import com.learninge.proto.app.v1.ResetPasswordRequest;
import com.learninge.proto.app.v1.RevokeOtherSessionsRequest;
import com.learninge.proto.app.v1.RevokeSessionRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gọi {@code learninge.app.v1.AuthService}. Chạy trên luồng {@code io}.
 */
public class AuthRemote {
    private final GrpcClient client;
    private final String deviceName;

    public AuthRemote(GrpcClient client, String deviceName) {
        this.client = client;
        this.deviceName = deviceName;
    }

    /**
     * EP-02.
     */
    public Result<RemoteSession> register(RegisterForm form) {
        RegisterRequest request = RegisterRequest.newBuilder()
                .setEmail(AccountRules.normalizeEmail(form.email()))
                .setNickname(form.nickname())
                .setDisplayName(form.displayName().strip())
                .setPassword(form.password())
                .setSecurityQuestionId(form.questionId())
                .setSecurityAnswer(form.answer().strip())
                .setDeviceName(deviceName)
                .build();
        return toSession(client.call(Rpc.REGISTER, AuthServiceGrpc::newBlockingStub, stub -> stub.register(request)));
    }

    /**
     * EP-03.
     */
    public Result<RemoteSession> login(String email, String password) {
        LoginRequest request = LoginRequest.newBuilder()
                .setEmail(AccountRules.normalizeEmail(email))
                .setPassword(password)
                .setDeviceName(deviceName)
                .build();
        return toSession(client.call(Rpc.LOGIN, AuthServiceGrpc::newBlockingStub, stub -> stub.login(request)));
    }

    /**
     * EP-04: thu hồi token của máy này.
     */
    public Result<Void> logout() {
        Result<?> result = client.call(Rpc.LOGOUT, AuthServiceGrpc::newBlockingStub,
                stub -> stub.logout(LogoutRequest.getDefaultInstance()));
        return result.isOk() ? Result.ok(null) : Result.errorOf(result);
    }

    /**
     * EP-17: các máy đang đăng nhập, dùng gần nhất trước.
     */
    public Result<List<DeviceSession>> sessions() {
        Result<ListSessionsResponse> response = client.call(Rpc.LIST_SESSIONS, AuthServiceGrpc::newBlockingStub,
                stub -> stub.listSessions(ListSessionsRequest.getDefaultInstance()));
        if (!response.isOk()) {
            return Result.errorOf(response);
        }
        return Result.ok(response.value.getSessionsList().stream().map(session -> new DeviceSession(session.getId(),
                session.getDeviceName(), session.getCreatedAtMs(),
                session.getLastUsedAtMs() == 0 ? null : session.getLastUsedAtMs(), session.getCurrent()))
                .collect(Collectors.toList()));
    }

    /**
     * EP-18.
     */
    public Result<Void> revokeSession(long id) {
        RevokeSessionRequest request = RevokeSessionRequest.newBuilder().setId(id).build();
        Result<?> result = client.call(Rpc.REVOKE_SESSION, AuthServiceGrpc::newBlockingStub,
                stub -> stub.revokeSession(request));
        return result.isOk() ? Result.ok(null) : Result.errorOf(result);
    }

    /**
     * EP-19.
     */
    public Result<Void> revokeOtherSessions() {
        Result<?> result = client.call(Rpc.REVOKE_OTHER_SESSIONS, AuthServiceGrpc::newBlockingStub,
                stub -> stub.revokeOtherSessions(RevokeOtherSessionsRequest.getDefaultInstance()));
        return result.isOk() ? Result.ok(null) : Result.errorOf(result);
    }

    /** EP-07: đổi mật khẩu, trả token mới. */
    public Result<Void> changePassword(String current, String newPassword) {
        ChangePasswordRequest request = ChangePasswordRequest.newBuilder()
                .setCurrentPassword(current)
                .setNewPassword(newPassword)
                .setDeviceName(deviceName)
                .build();
        Result<ChangePasswordResponse> result = client.call(Rpc.CHANGE_PASSWORD,
                AuthServiceGrpc::newBlockingStub, stub -> stub.changePassword(request));
        if (!result.isOk()) return Result.errorOf(result);
        return Result.ok(null);
    }

    /** EP-08: lấy câu hỏi bảo mật theo email. */
    public Result<Integer> getResetQuestion(String email) {
        GetResetQuestionRequest request = GetResetQuestionRequest.newBuilder()
                .setEmail(AccountRules.normalizeEmail(email))
                .build();
        Result<GetResetQuestionResponse> result = client.call(Rpc.GET_RESET_QUESTION,
                AuthServiceGrpc::newBlockingStub, stub -> stub.getResetQuestion(request));
        if (!result.isOk()) return Result.errorOf(result);
        return Result.ok(result.value.getSecurityQuestionId());
    }

    /** EP-09: đặt lại mật khẩu, trả session mới. */
    public Result<RemoteSession> resetPassword(String email, String answer, String newPassword) {
        ResetPasswordRequest request = ResetPasswordRequest.newBuilder()
                .setEmail(AccountRules.normalizeEmail(email))
                .setSecurityAnswer(answer)
                .setNewPassword(newPassword)
                .setDeviceName(deviceName)
                .build();
        return toSession(client.call(Rpc.RESET_PASSWORD, AuthServiceGrpc::newBlockingStub,
                stub -> stub.resetPassword(request)));
    }

    private static Result<RemoteSession> toSession(Result<AuthSession> result) {
        if (!result.isOk()) {
            return Result.errorOf(result);
        }
        AuthSession session = result.value;
        if (session.getToken().isEmpty() || session.getUser().getId().isEmpty()) {
            return Result.error(ErrorCode.BAD_RESPONSE);
        }
        return Result.ok(new RemoteSession(session.getToken(), session.getExpiresAtMs(), session.getUser().getId(),
                session.getUser().getEmail(), session.getUser().getNickname(), session.getUser().getDisplayName(),
                session.getUser().getCreatedAtMs()));
    }
}
