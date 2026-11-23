package com.learninge.app.data.remote;

import com.learninge.app.core.ErrorCode;
import com.learninge.app.core.Result;
import com.learninge.app.domain.rules.AccountRules;
import com.learninge.app.domain.rules.RegisterForm;
import com.learninge.proto.app.v1.AuthServiceGrpc;
import com.learninge.proto.app.v1.AuthSession;
import com.learninge.proto.app.v1.LoginRequest;
import com.learninge.proto.app.v1.LogoutRequest;
import com.learninge.proto.app.v1.RegisterRequest;

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
