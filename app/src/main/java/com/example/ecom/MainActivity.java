package com.example.ecom;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecom.core.api.ApiClient;
import com.example.ecom.core.data.SessionManager;
import com.example.ecom.core.navigation.AppRouter;

/**
 * ===== TRANG DUY NHẤT CỦA APP: CHỈ HIỂN THỊ NAVIGATION (≈ <RouterProvider> trong React) =====
 *
 *  - Chứa NavHost: nơi trang hiện tại được hiển thị
 *  - Nạp danh sách trang đã khai báo ở core/navigation/AppPages.java
 *  - Hết phiên đăng nhập (401) ở bất kỳ API nào -> về trang đăng nhập
 *
 * Không khai báo trang, không chứa header / bottom bar ở đây.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applyWindowInsets();

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        NavController nav = navHost.getNavController();

        Context app = getApplicationContext();
        AppRouter.install(nav, () -> new SessionManager(app).isLoggedIn());

        ApiClient.getInstance(this).setUnauthorizedListener(() -> AppRouter.toLogin(nav));
    }

    @Override
    protected void onDestroy() {
        ApiClient.getInstance(this).setUnauthorizedListener(null);
        super.onDestroy();
    }

    /** targetSdk 35+ bắt buộc edge-to-edge: chừa chỗ cho status bar / navigation bar / bàn phím. */
    private void applyWindowInsets() {
        View root = findViewById(R.id.navHostFragment);
        new WindowInsetsControllerCompat(getWindow(), root).setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
