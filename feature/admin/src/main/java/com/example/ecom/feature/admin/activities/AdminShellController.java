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
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.data.repository.AdminRepository;
/** Connects the admin header and footer to the existing single MainActivity. */
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
        bottom.setItemActiveIndicatorEnabled(false);
        bottom.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        bottom.setElevation(0);
        bottom.setOnItemSelectedListener(item-> {
            if(!syncing)AppRouter.toAdminTab(nav,item.getItemId());
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
            AdminRepository repo=AdminRepository.getInstance(activity);
            new MaterialAlertDialogBuilder(context()).setTitle(R.string.admin_notifications).setMessage(activity.getString(R.string.admin_notifications_info,repo.pendingCount())).setNegativeButton(R.string.admin_close,null).setPositiveButton(R.string.admin_view_orders,(d,which)->AppRouter.toAdminTab(nav,com.example.ecom.core.navigation.R.id.adminOrdersFragment)).show();
        }
        );
        header.findViewById(R.id.adminAccount).setOnClickListener(v->new MaterialAlertDialogBuilder(context()).setTitle(R.string.admin_account).setMessage(R.string.admin_account_info).setNegativeButton(R.string.admin_close,null).setPositiveButton(R.string.admin_title_settings,(d,which)->AppRouter.toAdminSettings(nav)).show());
    }
    private android.content.Context context() {
        return new ContextThemeWrapper(activity,R.style.ThemeOverlay_ShopAdmin);
    }
    public void apply(NavDestination destination,boolean isDetail) {
        detail=isDetail;
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
            if(item.getItemId()==R.id.adminExit)AppRouter.toLogin(nav);
            else if(item.getItemId()==com.example.ecom.core.navigation.R.id.adminSettingsFragment)AppRouter.toAdminSettings(nav);
            else AppRouter.toAdminTab(nav,item.getItemId());
            return true;
        }
        );
        menu.show();
    }
}
