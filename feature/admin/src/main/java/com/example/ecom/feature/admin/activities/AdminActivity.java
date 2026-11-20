package com.example.ecom.feature.admin.activities;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Server-backed admin host. The backend remains the authority for every operation. */
public final class AdminActivity extends AppCompatActivity {
    private AdminShellController shell;
    private boolean leaving;
    private boolean loggingOut;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        com.example.ecom.core.data.TokenManager tokens = new com.example.ecom.core.data.TokenManager(this);
        com.example.ecom.core.data.User user = tokens.getUser();
        if (!tokens.isLoggedIn() || user == null || !user.isAdmin()) {
            returnToApp();
            return;
        }
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        View root = findViewById(R.id.adminRoot);
        View statusBar = findViewById(R.id.adminStatusBar);
        new WindowInsetsControllerCompat(getWindow(), root).setAppearanceLightStatusBars(true);
        new WindowInsetsControllerCompat(getWindow(), root).setAppearanceLightNavigationBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, 0, bars.right, Math.max(bars.bottom, ime.bottom));
            android.view.ViewGroup.LayoutParams params = statusBar.getLayoutParams();
            params.height = bars.top;
            statusBar.setLayoutParams(params);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.adminNavHost);
        NavController nav = host.getNavController();
        View header = findViewById(R.id.adminHeader);
        BottomNavigationView bottom = findViewById(R.id.adminBottomNav);
        shell = new AdminShellController(this, header, bottom, nav);
        nav.addOnDestinationChangedListener((controller, destination, args) -> {
            boolean tab = AdminRouter.isTab(destination.getId());
            bottom.setVisibility(tab ? View.VISIBLE : View.GONE);
            shell.apply(destination, !tab);
        });
    }

    @Override protected void onResume() {
        super.onResume();
        if (leaving) return;
        if (!new com.example.ecom.core.data.TokenManager(this).isLoggedIn()) { returnToApp(); return; }
        com.example.ecom.core.api.ApiClient.getInstance(this).setUnauthorizedListener(this::expired);
        com.example.ecom.core.api.auth.AuthNetwork.get(this).setExpiredListener(this::expired);
    }

    @Override protected void onPause() {
        com.example.ecom.core.api.ApiClient.getInstance(this).setUnauthorizedListener(null);
        com.example.ecom.core.api.auth.AuthNetwork.get(this).setExpiredListener(null);
        super.onPause();
    }

    private void expired() {
        if (leaving || isFinishing()) return;
        android.widget.Toast.makeText(this, R.string.admin_session_expired, android.widget.Toast.LENGTH_LONG).show();
        returnToApp();
    }

    public void returnToApp() {
        if (leaving) return;
        leaving = true;
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    public void logout() {
        if (leaving || loggingOut) return;
        loggingOut = true;
        new com.example.ecom.core.api.auth.AuthRepository(this).logout(new com.example.ecom.core.api.ApiCallback<Object>() {
            public void onSuccess(Object data) { returnToApp(); }
            public void onError(String message, int status) { returnToApp(); }
        });
    }

    public void setHeaderTitle(CharSequence title) {
        if (shell != null) shell.setTitle(title);
        else {
            TextView header = findViewById(R.id.adminHeaderTitle);
            if (header != null) header.setText(title);
        }
    }
}
