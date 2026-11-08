package com.example.myapplication.frontend;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.R;
import com.example.myapplication.frontend.data.api.ApiClient;
import com.example.myapplication.frontend.navigation.AppRouter;
import com.example.myapplication.frontend.navigation.LayoutType;
import com.example.myapplication.frontend.navigation.RouteConfig;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity duy nhất (Single Activity). Đóng vai trò "App Shell":
 *  - chứa NavHost (nơi các trang được hiển thị)
 *  - đổi layout khung (header / back / bottom nav) theo từng trang
 *  - loading overlay dùng chung
 *  - xử lý hết phiên đăng nhập (401) toàn cục
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private View header;
    private TextView headerTitle;
    private ImageButton btnBack;
    private BottomNavigationView bottomNav;
    private View loadingOverlay;
    private int loadingCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        applyWindowInsets();

        header = findViewById(R.id.header);
        headerTitle = findViewById(R.id.textViewHeaderTitle);
        btnBack = findViewById(R.id.btnBack);
        bottomNav = findViewById(R.id.bottomNav);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        navController = navHost.getNavController();

        NavigationUI.setupWithNavController(bottomNav, navController);
        btnBack.setOnClickListener(v -> navController.navigateUp());
        navController.addOnDestinationChangedListener((controller, destination, args) -> applyLayout(destination));

        // Hết phiên đăng nhập ở bất kỳ API nào -> về màn đăng nhập
        ApiClient.getInstance(this).setUnauthorizedListener(() -> AppRouter.toLogin(navController));
    }

    @Override
    protected void onDestroy() {
        ApiClient.getInstance(this).setUnauthorizedListener(null);
        super.onDestroy();
    }

    private void applyLayout(NavDestination destination) {
        LayoutType layout = RouteConfig.layoutOf(destination.getId());
        header.setVisibility(layout.showHeader ? View.VISIBLE : View.GONE);
        btnBack.setVisibility(layout.showBack ? View.VISIBLE : View.GONE);
        bottomNav.setVisibility(layout.showBottomNav ? View.VISIBLE : View.GONE);
        headerTitle.setText(destination.getLabel());
    }

    /** Cho phép trang con đổi tiêu đề header (vd: tên sản phẩm). */
    public void setHeaderTitle(CharSequence title) {
        headerTitle.setText(title);
    }

    /** Loading dùng chung, hỗ trợ nhiều request song song. */
    public void setLoading(boolean loading) {
        loadingCount = Math.max(0, loadingCount + (loading ? 1 : -1));
        loadingOverlay.setVisibility(loadingCount > 0 ? View.VISIBLE : View.GONE);
    }

    /** targetSdk 35+ bắt buộc edge-to-edge: chừa chỗ cho status bar / navigation bar / bàn phím. */
    private void applyWindowInsets() {
        View root = findViewById(R.id.root);
        // Nền status bar là màu sáng -> icon status bar màu tối
        new WindowInsetsControllerCompat(getWindow(), root).setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
