package com.example.ecom;

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

import com.example.ecom.core.data.api.ApiClient;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.navigation.LayoutType;
import com.example.ecom.core.navigation.RouteConfig;
import com.example.ecom.core.ui.base.AppShell;
import com.example.ecom.feature.admin.activities.AdminShellController;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity duy nhất (Single Activity). Đóng vai trò "App Shell":
 *  - chứa NavHost (nơi các trang được hiển thị)
 *  - đổi layout khung (header / back / bottom nav) theo từng trang
 *  - loading overlay dùng chung
 *  - xử lý hết phiên đăng nhập (401) toàn cục
 */
public class MainActivity extends AppCompatActivity implements AppShell {

    private NavController navController;
    private View header;
    private TextView headerTitle;
    private ImageButton btnBack;
    private BottomNavigationView bottomNav;
    private View loadingOverlay;
    private int loadingCount = 0;
    private View adminHeader, adminStatusBar;
    private BottomNavigationView adminBottomNav;
    private AdminShellController adminShell;
    private boolean adminMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        applyWindowInsets();

        header = findViewById(R.id.header);
        headerTitle = findViewById(R.id.textViewHeaderTitle);
        btnBack = findViewById(R.id.btnBack);
        bottomNav = findViewById(R.id.bottomNav);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        adminHeader = findViewById(R.id.adminHeader);
        adminStatusBar = findViewById(R.id.adminStatusBar);
        adminBottomNav = findViewById(R.id.adminBottomNav);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        navController = navHost.getNavController();
        adminShell = new AdminShellController(this, adminHeader, adminBottomNav, navController);

        NavigationUI.setupWithNavController(bottomNav, navController);
        btnBack.setOnClickListener(v -> navController.navigateUp());
        navController.addOnDestinationChangedListener((controller, destination, args) -> applyLayout(destination));

        // Hết phiên đăng nhập ở bất kỳ API nào -> về màn đăng nhập
        ApiClient.getInstance(this).setUnauthorizedListener(() -> AppRouter.toLogin(navController));
        if (savedInstanceState == null && getIntent().getBooleanExtra("admin_preview", false)) {
            AppRouter.toAdminDashboard(navController);
        }
    }

    @Override
    protected void onDestroy() {
        ApiClient.getInstance(this).setUnauthorizedListener(null);
        super.onDestroy();
    }

    private void applyLayout(NavDestination destination) {
        LayoutType layout = RouteConfig.layoutOf(destination.getId());
        adminMode = layout.isAdmin;
        header.setVisibility(!adminMode && layout.showHeader ? View.VISIBLE : View.GONE);
        btnBack.setVisibility(layout.showBack ? View.VISIBLE : View.GONE);
        bottomNav.setVisibility(!adminMode && layout.showBottomNav ? View.VISIBLE : View.GONE);
        adminHeader.setVisibility(adminMode ? View.VISIBLE : View.GONE);
        adminBottomNav.setVisibility(adminMode && layout.showBottomNav ? View.VISIBLE : View.GONE);
        adminStatusBar.setVisibility(adminMode ? View.VISIBLE : View.GONE);
        if (adminMode) adminShell.apply(destination, layout.showBack);
        View root = findViewById(R.id.root);
        android.util.TypedValue surface = new android.util.TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, surface, true);
        root.setBackgroundColor(adminMode ? getColor(com.example.ecom.feature.admin.R.color.admin_background)
                : surface.resourceId != 0 ? getColor(surface.resourceId) : surface.data);
        new WindowInsetsControllerCompat(getWindow(), root).setAppearanceLightStatusBars(!adminMode);
        new WindowInsetsControllerCompat(getWindow(), root).setAppearanceLightNavigationBars(true);
        ViewCompat.requestApplyInsets(root);
        headerTitle.setText(destination.getLabel());
    }

    @Override
    public void setHeaderTitle(CharSequence title) {
        if (headerTitle != null) headerTitle.setText(title);
        if (adminMode && adminShell != null) adminShell.setTitle(title);
    }

    @Override
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
            v.setPadding(bars.left, adminMode ? 0 : bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            if (adminStatusBar != null) {
                android.view.ViewGroup.LayoutParams params = adminStatusBar.getLayoutParams();
                params.height = bars.top;
                adminStatusBar.setLayoutParams(params);
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
