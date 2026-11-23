package com.learninge.app.data.remote;

/**
 * Phiên máy chủ cấp sau khi đăng ký/đăng nhập (BR-057), dạng model của app.
 */
public record RemoteSession(String token, long expiresAtMillis, String userId, String email, String nickname,
                            String displayName, long createdAtMillis) {

    @Override
    public String toString() {
        // Không để token lọt vào log.
        return "RemoteSession";
    }
}
