package com.learninge.app.domain.rules;

/**
 * Dữ liệu người dùng nhập ở SCR-03. {@code questionId} 1..5, 0 = chưa chọn (BR-009).
 */
public record RegisterForm(String email, String nickname, String displayName, String password, String confirm,
                           int questionId, String answer) {

    @Override
    public String toString() {
        // Không để mật khẩu lọt vào log.
        return "RegisterForm";
    }
}
