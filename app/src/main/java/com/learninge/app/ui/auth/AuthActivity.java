package com.learninge.app.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.learninge.app.R;

/**
 * Luồng chưa đăng nhập (SCR-01, 02, 03, 25). Tách khỏi {@code MainActivity} để nút back không quay lại màn đăng nhập.
 */
public class AuthActivity extends AppCompatActivity {

    /**
     * Mở lại luồng đăng nhập và xóa ngăn xếp của luồng chính.
     */
    public static Intent newTask(Context context) {
        return new Intent(context, AuthActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        View root = findViewById(R.id.nav_host_auth);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, 0, bars.right, bars.bottom);
            return insets;
        });
    }
}
