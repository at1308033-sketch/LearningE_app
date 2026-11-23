package com.learninge.app.ui.auth;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.learninge.app.BuildConfig;
import com.learninge.app.R;
import com.learninge.app.core.ErrorCode;

/**
 * Luồng chưa đăng nhập (SCR-01, 02, 03, 25). Tách khỏi {@code MainActivity} để nút back không quay lại màn đăng nhập.
 */
public class AuthActivity extends AppCompatActivity {

    private static final String EXTRA_REASON = "reason";

    /**
     * Mở lại luồng đăng nhập và xóa ngăn xếp của luồng chính. {@code reason} null = người dùng tự đăng xuất.
     */
    public static Intent newTask(Context context, @Nullable ErrorCode reason) {
        return new Intent(context, AuthActivity.class)
                .putExtra(EXTRA_REASON, reason == null ? null : reason.name())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    /**
     * Vì sao phiên trước kết thúc, null nếu không phải do máy chủ.
     */
    @Nullable
    ErrorCode endReason() {
        String name = getIntent().getStringExtra(EXTRA_REASON);
        return name == null ? null : ErrorCode.valueOf(name);
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
        requestLocalNetworkForDebug();
    }

    // Android 17 (API 37) chặn mọi kết nối tới mạng nội bộ, kể cả 10.0.2.2 của emulator, nếu chưa có quyền này.
    // Chỉ bản debug gọi máy chủ nội bộ; bản release gọi máy chủ công khai qua TLS nên không cần.
    private void requestLocalNetworkForDebug() {
        boolean isNeeded = BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN;
        if (isNeeded && checkSelfPermission(Manifest.permission.ACCESS_LOCAL_NETWORK)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_LOCAL_NETWORK}, 0);
        }
    }
}
