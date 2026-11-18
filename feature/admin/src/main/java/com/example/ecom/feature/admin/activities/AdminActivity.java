package com.example.ecom.feature.admin.activities;

import android.os.Bundle;
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

/** Hosts the existing admin preview without changing the customer activity or login flow. */
public final class AdminActivity extends AppCompatActivity {
    private AdminShellController shell;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        androidx.activity.EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        View root = findViewById(R.id.adminRoot);
        View statusBar = findViewById(R.id.adminStatusBar);
        new WindowInsetsControllerCompat(getWindow(), root).setAppearanceLightStatusBars(false);
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

    public void setHeaderTitle(CharSequence title) {
        if (shell != null) shell.setTitle(title);
        else {
            TextView header = findViewById(R.id.adminHeaderTitle);
            if (header != null) header.setText(title);
        }
    }
}
