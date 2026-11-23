package com.learninge.app.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.learninge.app.LearningEApp;
import com.learninge.app.NavMainDirections;
import com.learninge.app.R;
import com.learninge.app.core.SessionEvents;
import com.learninge.app.databinding.ActivityMainBinding;
import com.learninge.app.ui.auth.AuthActivity;
import com.learninge.app.ui.common.StatusBarHost;

import java.util.Set;

/**
 * App chính: 5 tab, mỗi tab giữ ngăn xếp riêng; thanh tab chỉ hiện ở 5 màn hình gốc (UI-UX-SPECIFICATION). Thanh
 * "Đang ngoại tuyến" hiện ở các màn cần mạng và đẩy nội dung xuống.
 */
public class MainActivity extends AppCompatActivity implements StatusBarHost {
    private static final Set<Integer> TAB_ROOTS = Set.of(R.id.home_fragment, R.id.learn_fragment,
            R.id.practice_fragment, R.id.search_fragment, R.id.me_fragment);
    private static final Set<Integer> ONLINE_SCREENS = Set.of(R.id.home_fragment, R.id.me_fragment,
            R.id.progress_fragment, R.id.leaderboard_fragment, R.id.sync_fragment);

    private ActivityMainBinding binding;
    private int bottomInset;
    private boolean isOnline = true;
    private boolean isOnlineScreen;
    private boolean isDarkHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // BR-059: phiên bị máy chủ kết thúc thì về đăng nhập, xóa ngăn xếp.
        SessionEvents events = LearningEApp.services(this).sessionEvents();
        events.ended().observe(this, reason -> {
            if (reason != null) {
                events.consume();
                startActivity(AuthActivity.newTask(this, reason));
            }
        });

        NavHostFragment host = binding.navHostMain.getFragment();
        NavController navController = host.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            boolean isTabRoot = TAB_ROOTS.contains(destination.getId());
            binding.bottomNav.setVisibility(isTabRoot ? View.VISIBLE : View.GONE);
            applyBottomPadding();
            isOnlineScreen = ONLINE_SCREENS.contains(destination.getId());
            updateOfflineBanner();
        });
        LearningEApp.services(this).networkMonitor().online().observe(this, online -> {
            isOnline = online;
            updateOfflineBanner();
        });
        binding.llOffline.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() != R.id.sync_fragment) {
                navController.navigate(NavMainDirections.actionGlobalSync());
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> applyInsets(insets));
    }

    @Override
    protected void onStart() {
        super.onStart();
        LearningEApp.services(this).syncScheduler().onForeground();
    }

    @Override
    public void setDarkHeader(boolean isDark) {
        isDarkHeader = isDark;
        updateStatusBarIcons();
    }

    private void updateOfflineBanner() {
        int visibility = !isOnline && isOnlineScreen ? View.VISIBLE : View.GONE;
        if (binding.llOffline.getVisibility() != visibility) {
            binding.llOffline.setVisibility(visibility);
            ViewCompat.requestApplyInsets(binding.getRoot());
            updateStatusBarIcons();
        }
    }

    // Thanh ngoại tuyến có nền sáng nên icon thanh trạng thái phải màu tối.
    private void updateStatusBarIcons() {
        boolean hasLightBackground = !isDarkHeader || isBannerShown();
        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(hasLightBackground);
    }

    private WindowInsetsCompat applyInsets(WindowInsetsCompat insets) {
        Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
        binding.getRoot().setPadding(bars.left, 0, bars.right, 0);
        bottomInset = bars.bottom;
        applyBottomPadding();
        if (!isBannerShown()) {
            return insets;
        }
        // Thanh ngoại tuyến đã chừa chỗ cho thanh trạng thái nên màn hình bên dưới không chừa nữa.
        Insets system = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        binding.llOffline.setPadding(binding.llOffline.getPaddingLeft(), system.top,
                binding.llOffline.getPaddingRight(), 0);
        return new WindowInsetsCompat.Builder(insets)
                .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.NONE)
                .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.NONE)
                .build();
    }

    private boolean isBannerShown() {
        return binding.llOffline.getVisibility() == View.VISIBLE;
    }

    // Thanh tab tự chừa chỗ cho thanh điều hướng hệ thống; khi ẩn thanh tab thì nội dung phải tự chừa.
    private void applyBottomPadding() {
        boolean tabVisible = binding.bottomNav.getVisibility() == View.VISIBLE;
        binding.navHostMain.setPadding(0, 0, 0, tabVisible ? 0 : bottomInset);
    }
}
