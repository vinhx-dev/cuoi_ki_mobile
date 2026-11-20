package com.example.ecom.feature.admin.activities;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.navigation.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecom.feature.admin.AdminViewModel;
/** Connects the admin header and footer within AdminActivity. */
public final class AdminShellController {
    private final AppCompatActivity activity;
    private final View header;
    private final BottomNavigationView bottom;
    private final NavController nav;
    private boolean syncing,detail;
    public AdminShellController(AppCompatActivity activity,View header,BottomNavigationView bottom,NavController nav) {
        this.activity=activity;
        this.header=header;
        this.bottom=bottom;
        this.nav=nav;
        bottom.setItemActiveIndicatorEnabled(true);
        bottom.setElevation(0);
        bottom.setOnItemSelectedListener(item-> {
            if(!syncing)AdminRouter.toAdminTab(nav,item.getItemId());
            return true;
        }
        );
        bottom.setOnItemReselectedListener(item-> {
        }
        );
        header.findViewById(R.id.adminMenu).setOnClickListener(v-> {
            if(detail)activity.getOnBackPressedDispatcher().onBackPressed();
            else showMenu(v);
        }
        );
        header.findViewById(R.id.adminNotifications).setOnClickListener(v-> {
            androidx.fragment.app.Fragment host=activity.getSupportFragmentManager().findFragmentById(R.id.adminNavHost);
            androidx.fragment.app.Fragment page=host==null?null:host.getChildFragmentManager().getPrimaryNavigationFragment();
            if(page instanceof com.example.ecom.feature.admin.fragments.AdminFragment)
                ((com.example.ecom.feature.admin.fragments.AdminFragment)page).refreshData();
        }
        );
        header.findViewById(R.id.adminAccount).setOnClickListener(v -> {
            com.example.ecom.core.data.TokenManager tokens=new com.example.ecom.core.data.TokenManager(activity);
            new MaterialAlertDialogBuilder(context()).setTitle(R.string.admin_account)
                .setMessage(tokens.getUserName()+"\n"+tokens.getUserEmail())
                .setNegativeButton(R.string.admin_close,null)
                .setPositiveButton(R.string.admin_logout,(d,which) -> ((AdminActivity)activity).logout()).show();
        });
    }
    private android.content.Context context() {
        return new ContextThemeWrapper(activity,R.style.ThemeOverlay_ShopAdmin);
    }
    public void apply(NavDestination destination,boolean isDetail) {
        detail=isDetail;
        header.findViewById(R.id.adminNotifications).setVisibility(destination.getId()==R.id.adminSettingsFragment?View.INVISIBLE:View.VISIBLE);
        ImageButton menu=header.findViewById(R.id.adminMenu);
        menu.setImageResource(detail?R.drawable.admin_ic_back:R.drawable.admin_ic_menu);
        menu.setContentDescription(activity.getString(detail?R.string.admin_back:R.string.admin_menu));
        ((TextView)header.findViewById(R.id.adminHeaderTitle)).setText(destination.getLabel());
        if(bottom.getMenu().findItem(destination.getId())!=null) {
            syncing=true;
            bottom.setSelectedItemId(destination.getId());
            syncing=false;
        }
    }
    public void setTitle(CharSequence title) {
        ((TextView)header.findViewById(R.id.adminHeaderTitle)).setText(title);
    }
    private void showMenu(View anchor) {
        PopupMenu menu=new PopupMenu(context(),anchor);
        menu.inflate(R.menu.admin_menu);
        menu.setOnMenuItemClickListener(item-> {
            if(item.getItemId()==R.id.adminExit)((AdminActivity)activity).logout();
            else if(item.getItemId()==R.id.adminSettingsFragment)AdminRouter.toAdminSettings(nav);
            else AdminRouter.toAdminTab(nav,item.getItemId());
            return true;
        }
        );
        menu.show();
    }
}
