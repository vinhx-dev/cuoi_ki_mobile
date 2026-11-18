package com.example.ecom.feature.admin.fragments;
import android.view.View;
import android.util.Patterns;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.example.ecom.feature.admin.R;
public class AdminSettingsFragment extends AdminFragment {
    @Override protected int layout() {
        return R.layout.fragment_admin_settings;
    }
    @Override protected void bind(View view) {
        TextInputLayout name=view.findViewById(R.id.inputShopNameLayout),email=view.findViewById(R.id.inputShopEmailLayout);
        MaterialSwitch notifications=view.findViewById(R.id.switchNotifications);
        name.getEditText().setText(repository.setting("name","Shop Admin"));
        email.getEditText().setText(repository.setting("email","admin@shop.vn"));
        notifications.setChecked(repository.notifications());
        view.findViewById(R.id.btnSaveSettings).setOnClickListener(v-> {
            String n=name.getEditText().getText().toString().trim(),e=email.getEditText().getText().toString().trim();
            name.setError(null);
            email.setError(null);
            if(n.isEmpty()) {
                name.setError(getString(R.string.admin_error_required));
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                email.setError(getString(R.string.admin_error_email));
                return;
            }
            repository.saveSettings(n,e,notifications.isChecked());
            toast(getString(R.string.admin_settings_saved));
        }
        );
    }
}
