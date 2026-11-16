package com.example.ecom;
import android.app.*;
import android.os.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.TextView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.data.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicReference;
/** Device smoke tests without extra test dependencies or a backend login bypass in production. */
public final class CustomerUiSmokeTest extends Instrumentation {
    private MainActivity activity;
    private NavController nav;
    private View decor() {
        return activity.getWindow().getDecorView();
    }
    private View text(View v,String target) {
        if(v instanceof TextView&&target.equals(((TextView)v).getText().toString()))return v;
        if(v instanceof ViewGroup) {
            ViewGroup g=(ViewGroup)v;
            for(int i=0;i<g.getChildCount();i++) {
                View found=text(g.getChildAt(i),target);
                if(found!=null)return found;
            }
        }
        return null;
    }
    private View find(String target) {
        AtomicReference<View> result=new AtomicReference<>();
        runOnMainSync(()->result.set(text(decor(),target)));
        return result.get();
    }
    private void await(String target) {
        long end=SystemClock.uptimeMillis()+25000;
        while(SystemClock.uptimeMillis()<end) {
            waitForIdleSync();
            if(find(target)!=null)return;
            SystemClock.sleep(100);
        }
        throw new AssertionError("Missing UI text: "+target);
    }
    private void click(String target) {
        await(target);
        runOnMainSync(()-> {
            View v=text(decor(),target);
            while(v!=null&&!v.isClickable())v=(v.getParent() instanceof View)?(View)v.getParent():null;
            if(v==null)throw new AssertionError("Not clickable: "+target);
            v.performClick();
        }
        );
        waitForIdleSync();
    }
    private void check(boolean ok,String message) {
        if(!ok)throw new AssertionError(message);
    }
    private void shot(String name)throws Exception {
        File dir=new File(getTargetContext().getExternalFilesDir(null),"customer-ui-qa");
        dir.mkdirs();
        Bitmap bitmap=getUiAutomation().takeScreenshot();
        try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG,100,out);
        }
        bitmap.recycle();
    }
    @Override public void onCreate(Bundle args) {
        super.onCreate(args);
        start();
    }
    @Override public void onStart() {
        Bundle result=new Bundle();
        try {
            android.content.Context testStore=new android.content.ContextWrapper(getTargetContext()) {
                @Override public android.content.Context getApplicationContext() {
                    return this;
                }
                @Override public android.content.SharedPreferences getSharedPreferences(String name,int mode) {
                    return super.getSharedPreferences("customer_smoke_"+name,mode);
                }
            }
            ;
            testStore.getSharedPreferences("customer_cart_",0).edit().clear().commit();
            // Domain invariants, independent from server data.
            check(ShopCatalog.filter(ShopCatalog.samples(),"Váy","").size()==1,"Category filter");
            check(ShopCatalog.filter(ShopCatalog.samples(),"Giày","cotton").isEmpty(),"Category and search intersection");
            ShopProduct fixture=new ShopProduct(-999,"Test",150000,2,"Áo","","");
            check(!LocalCart.add(testStore,fixture,"",1),"A size is required");
            check(LocalCart.add(testStore,fixture,"M",1),"First cart item");
            check(!LocalCart.add(testStore,fixture,"L",2),"Inventory across sizes");
            check(LocalCart.add(testStore,fixture,"L",1),"Second cart item");
            check(LocalCart.quantity(testStore,-999)==2,"Persistent cart count");
            testStore.getSharedPreferences("customer_cart_",0).edit().clear().commit();
            Intent launch=new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity=(MainActivity)startActivitySync(launch);
            SystemClock.sleep(800);
            waitForIdleSync();
            runOnMainSync(()-> {
                nav=((NavHostFragment)activity.getSupportFragmentManager().findFragmentById(R.id.navHostFragment)).getNavController();
                com.example.ecom.core.api.ApiClient.getInstance(activity).setUnauthorizedListener(null);
                AppRouter.install(nav,()->true);
                nav.navigate("home");
            }
            );
            await("Danh mục");
            await("Gợi ý cho bạn");
            await("Áo Khoác Hoodie");
            SystemClock.sleep(1500);
            shot("01-home");
            click("Áo Khoác Hoodie");
            await("Mua ngay");
            runOnMainSync(()->check(decor().findViewById(com.example.ecom.core.ui.R.id.bottomBar)==null,"Home to detail"));
            runOnMainSync(()->nav.navigateUp());
            await("Danh mục");
            click("Áo");
            await("Sản phẩm: Áo");
            runOnMainSync(()->check(decor().findViewById(com.example.ecom.core.ui.R.id.bottomBar)!=null,"List bottom navigation"));
            await("Áo Thun Cotton");
            SystemClock.sleep(400);
            shot("02-product-list");
            await("Áo Thun Cotton");
            click("Áo Thun Cotton");
            await("Áo Thun Cotton");
            runOnMainSync(()->check(decor().findViewById(com.example.ecom.core.ui.R.id.bottomBar)==null,"Detail must have no bottom navigation"));
            SystemClock.sleep(400);
            shot("03-detail");
            click("Mua ngay");
            awaitDialog("Chọn size");
            shot("04-sheet");
            // Dialog window text is in a separate root, accessed from the actual DialogFragment.
            runOnMainSync(()-> {
                androidx.fragment.app.Fragment page=((NavHostFragment)activity.getSupportFragmentManager().findFragmentById(R.id.navHostFragment)).getChildFragmentManager().getPrimaryNavigationFragment();
                androidx.fragment.app.DialogFragment sheet=(androidx.fragment.app.DialogFragment)page.getChildFragmentManager().findFragmentByTag("product_options");
                View root=sheet.getDialog().getWindow().getDecorView();
                text(root,"Xác nhận").performClick();
                check(text(root,"Vui lòng chọn size trước khi xác nhận.")!=null,"Size validation");
                text(root,"M").performClick();
                text(root,"+").performClick();
                check(text(root,"300.000 ₫")!=null,"Total updates");
                text(root,"−").performClick();
                text(root,"−").performClick();
                check(text(root,"150.000 ₫")!=null,"Quantity minimum");
                text(root,"Xác nhận").performClick();
                check(text(root,"Đây là sản phẩm mẫu. Cần kết nối cửa hàng để đặt mua; chưa tạo đơn hàng.")!=null,"No fake checkout");
            }
            );
            shot("05-sheet-selected");
            runOnMainSync(()-> {
                androidx.fragment.app.Fragment page=((NavHostFragment)activity.getSupportFragmentManager().findFragmentById(R.id.navHostFragment)).getChildFragmentManager().getPrimaryNavigationFragment();
                ((androidx.fragment.app.DialogFragment)page.getChildFragmentManager().findFragmentByTag("product_options")).dismiss();
            }
            );
            waitForIdleSync();
            runOnMainSync(()->nav.navigateUp());
            await("Sản phẩm: Áo");
            runOnMainSync(()->decor().findViewById(com.example.ecom.core.ui.R.id.tabHome).performClick());
            await("Danh mục");
            result.putString("stream","PASS: catalogue filters, cart stock, Home/category navigation, detail footer, sheet size/quantity/total, sample checkout guard, Back. Screenshots in external-files/customer-ui-qa.\n");
            finish(Activity.RESULT_OK,result);
        }
        catch(Throwable failure) {
            result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(failure));
            finish(Activity.RESULT_CANCELED,result);
        }
    }
    private void awaitDialog(String target) {
        long end=SystemClock.uptimeMillis()+5000;
        while(SystemClock.uptimeMillis()<end) {
            AtomicReference<Boolean> ok=new AtomicReference<>(false);
            runOnMainSync(()-> {
                androidx.fragment.app.Fragment page=((NavHostFragment)activity.getSupportFragmentManager().findFragmentById(R.id.navHostFragment)).getChildFragmentManager().getPrimaryNavigationFragment();
                androidx.fragment.app.Fragment f=page.getChildFragmentManager().findFragmentByTag("product_options");
                if(f instanceof androidx.fragment.app.DialogFragment) {
                    Dialog d=((androidx.fragment.app.DialogFragment)f).getDialog();
                    ok.set(d!=null&&text(d.getWindow().getDecorView(),target)!=null);
                }
            }
            );
            if(ok.get())return;
            SystemClock.sleep(100);
        }
        throw new AssertionError("Missing dialog: "+target);
    }
}
