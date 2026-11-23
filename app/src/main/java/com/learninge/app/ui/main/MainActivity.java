package com.learninge.app.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.learninge.app.R;
import com.learninge.app.databinding.ActivityMainBinding;

import java.util.Set;

/**
 * App chính: 5 tab, mỗi tab giữ ngăn xếp riêng; thanh tab chỉ hiện ở 5 màn hình gốc (UI-UX-SPECIFICATION).
 */
public class MainActivity extends AppCompatActivity {
    private static final Set<Integer> TAB_ROOTS = Set.of(R.id.home_fragment, R.id.learn_fragment,
            R.id.practice_fragment, R.id.search_fragment, R.id.me_fragment);

    private ActivityMainBinding binding;
    private int bottomInset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment host = binding.navHostMain.getFragment();
        NavController navController = host.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            boolean isTabRoot = TAB_ROOTS.contains(destination.getId());
            binding.bottomNav.setVisibility(isTabRoot ? View.VISIBLE : View.GONE);
            applyBottomPadding();
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, 0, bars.right, 0);
            bottomInset = bars.bottom;
            applyBottomPadding();
            return insets;
        });
    }

    // Thanh tab tự chừa chỗ cho thanh điều hướng hệ thống; khi ẩn thanh tab thì nội dung phải tự chừa.
    private void applyBottomPadding() {
        boolean tabVisible = binding.bottomNav.getVisibility() == View.VISIBLE;
        binding.navHostMain.setPadding(0, 0, 0, tabVisible ? 0 : bottomInset);
    }
}
