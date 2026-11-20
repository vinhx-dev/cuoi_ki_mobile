package com.example.ecom;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.view.View;
import android.widget.TextView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.auth.AuthRepository;
import com.example.ecom.core.data.*;
import com.example.ecom.feature.admin.activities.AdminActivity;
import com.example.ecom.feature.admin.data.*;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import com.google.android.material.textfield.TextInputLayout;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

/** Live, read-only smoke test. Requires a real saved admin session; never seeds data or tokens. */
public final class AdminIntegrationSmokeTest extends Instrumentation {
    private AdminActivity activity;
    private NavController nav;
    private boolean productCrud;
    private boolean screenshots;
    private boolean imagesOnly;
    @Override public void onCreate(Bundle args) {
        super.onCreate(args); productCrud="true".equals(args.getString("productCrud"));
        screenshots="true".equals(args.getString("screenshots"));
        imagesOnly="true".equals(args.getString("imagesOnly"));
        start();
    }
    private void screenshot(String name) throws Exception {
        if(!screenshots) return;
        waitForIdleSync(); SystemClock.sleep(name.startsWith("01-")?2300:150);
        android.graphics.Bitmap bitmap=getUiAutomation().takeScreenshot();
        if(bitmap==null) return;
        java.io.File directory=new java.io.File(getTargetContext().getExternalFilesDir(null),"admin-ui-qa");
        directory.mkdirs();
        try(java.io.FileOutputStream stream=new java.io.FileOutputStream(new java.io.File(directory,name+".png"))) {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,stream);
        } finally { bitmap.recycle(); }
    }
    private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    private <T> T request(Consumer<ApiCallback<T>> call) throws Exception {
        CountDownLatch done=new CountDownLatch(1);
        AtomicReference<T> value=new AtomicReference<>();
        AtomicReference<String> error=new AtomicReference<>();
        call.accept(new ApiCallback<T>() {
            public void onSuccess(T data) { value.set(data); done.countDown(); }
            public void onError(String message,int status) { error.set("HTTP "+status+": "+message); done.countDown(); }
        });
        check(done.await(45,TimeUnit.SECONDS),"API request timed out");
        if(error.get()!=null) throw new AssertionError(error.get());
        return value.get();
    }
    private void waitUi(BooleanSupplier condition,String message) throws Exception {
        long end=SystemClock.uptimeMillis()+45000;
        while(SystemClock.uptimeMillis()<end) {
            AtomicBoolean ok=new AtomicBoolean();
            runOnMainSync(() -> ok.set(condition.getAsBoolean()));
            if(ok.get()) return;
            SystemClock.sleep(100);
        }
        throw new AssertionError(message);
    }
    private boolean text(int id,String expected) {
        TextView view=activity.findViewById(id);
        return view!=null && view.isShown() && view.getText().toString().contains(expected);
    }
    private void tab(int id) {
        runOnMainSync(() -> AdminRouter.toAdminTab(nav,id));
        waitForIdleSync();
    }
    private void back() { runOnMainSync(() -> activity.getOnBackPressedDispatcher().onBackPressed()); waitForIdleSync(); }
    @Override public void onStart() {
        Bundle result=new Bundle();
        AdminApi api=new AdminApi(getTargetContext());
        AdminProduct temporary=null;
        try {
            String image=checkInlineImages();
            if(imagesOnly) {
                result.putString("stream","PASS: local PNG/JPEG encoding, resize, Base64 preview and product image omission/clear semantics.\n");
                finish(Activity.RESULT_OK,result); return;
            }
            TokenManager tokens=new TokenManager(getTargetContext());
            User saved=tokens.getUser();
            if(!tokens.isLoggedIn() || saved==null || !saved.isAdmin()) {
                result.putString("stream","SKIP: Cần đăng nhập ROLE_ADMIN trên emulator trước khi chạy test.\n");
                finish(Activity.RESULT_CANCELED,result); return;
            }
            User me=this.<User>request(cb -> new AuthRepository(getTargetContext()).me(cb));
            check(me.isAdmin(),"Server did not confirm ROLE_ADMIN");
            JSONObject dashboard=this.<JSONObject>request(api::dashboard);
            List<AdminCategory> categories=this.<List<AdminCategory>>request(api::categories);
            if(productCrud) {
                AdminProduct qa=new AdminProduct();
                qa.name="QA admin integration "+UUID.randomUUID();
                qa.price=new java.math.BigDecimal("123456.78"); qa.stock=3; qa.soldCount=0;
                qa.description="Temporary integration check"; qa.sizes.add("QA-custom-size");
                qa.image=image;
                qa.categoryId=categories.isEmpty()?0:categories.get(0).id;
                qa.forAI=new JSONObject().put("integrationCheck",true).put("values",new JSONArray().put(1).put("test"));
                temporary=this.<AdminProduct>request(cb -> api.saveProduct(qa,cb));
                check(temporary.price.compareTo(qa.price)==0 && temporary.sizes.equals(qa.sizes),"POST product mapping");
                check(temporary.forAI.toString().equals(qa.forAI.toString()),"forAI JSON lost");
                check(image.equals(temporary.image),"POST did not return Base64 image");
                temporary.price=new java.math.BigDecimal("234567.89"); temporary.stock=4;
                temporary.includeImage=false;
                AdminProduct update=temporary;
                temporary=this.<AdminProduct>request(cb -> api.saveProduct(update,cb));
                check(temporary.stock==4 && temporary.price.compareTo(update.price)==0,"PUT product mapping");
                check(image.equals(temporary.image),"PUT without image did not preserve Base64");
                AdminProduct fetched=this.<AdminProduct>request(cb -> api.product(update.id,cb));
                check(image.equals(fetched.image),"GET did not return persisted Base64");
                checkImageApiContract(temporary,image);
            }
            List<AdminProduct> products=this.<List<AdminProduct>>request(api::products);
            List<AdminOrder> orders=this.<List<AdminOrder>>request(cb -> api.orders(null,null,cb));
            List<AdminCustomer> users=this.<List<AdminCustomer>>request(cb -> api.users("",null,cb));
            this.<JSONArray>request(api::topProducts);
            this.<JSONArray>request(api::categoryStats);
            for(String status:new String[]{"PENDING","SHIPPING","DELIVERED","CANCELLED"}) {
                List<AdminOrder> filtered=this.<List<AdminOrder>>request(cb -> api.orders(status,null,cb));
                for(AdminOrder order:filtered) check(status.equals(order.status),"Status filter ignored");
            }
            if(!products.isEmpty()) {
                AdminProduct product=this.<AdminProduct>request(cb -> api.product(products.get(0).id,cb));
                check(product.body().getJSONArray("size").length()==product.sizes.size(),"Size mapping");
                check(product.price.compareTo(new java.math.BigDecimal(product.body().get("price").toString()))==0,"Price precision");
                check(product.body().has("forAI") && product.body().has("soldCount"),"Admin fields lost");
            }
            if(!orders.isEmpty()) {
                AdminOrder order=this.<AdminOrder>request(cb -> api.order(orders.get(0).id,cb));
                check(order.userEmail!=null && order.userAddress!=null,"Recipient mapping");
                check(!"CONFIRMED".equals(order.nextStatus()),"Invalid legacy transition");
            }
            if(!users.isEmpty()) {
                long id=users.get(0).id;
                JSONObject user=this.<JSONObject>request(cb -> api.user(id,cb));
                check(user.has("user") && user.has("stats"),"User detail DTO");
                List<AdminOrder> filtered=this.<List<AdminOrder>>request(cb -> api.orders(null,id,cb));
                for(AdminOrder order:filtered) check(order.customerId==id,"User order filter ignored");
            }
            Instrumentation.ActivityMonitor monitor=addMonitor(AdminActivity.class.getName(),null,false);
            Intent launch=new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivitySync(launch);
            activity=(AdminActivity)waitForMonitorWithTimeout(monitor,45000); removeMonitor(monitor);
            check(activity!=null,"Session restoration did not open AdminActivity");
            runOnMainSync(() -> nav=((NavHostFragment)activity.getSupportFragmentManager()
                .findFragmentById(com.example.ecom.feature.admin.R.id.adminNavHost)).getNavController());
            waitUi(() -> text(com.example.ecom.feature.admin.R.id.metricOrders,String.valueOf(dashboard.optLong("totalOrders"))),"Dashboard did not load");
            screenshot("01-dashboard");
            tab(com.example.ecom.feature.admin.R.id.adminProductsFragment);
            waitUi(() -> text(com.example.ecom.feature.admin.R.id.textResultCount,products.size()+" kết quả"),"Product list did not load");
            screenshot("02-products");
            runOnMainSync(() -> AdminRouter.toAdminProductForm(navHost(),0));
            waitUi(() -> { View field=activity.findViewById(com.example.ecom.feature.admin.R.id.inputName); return field!=null && field.isShown(); },"Product form did not load");
            screenshot("03-product-form");
            runOnMainSync(() -> {
                activity.findViewById(com.example.ecom.feature.admin.R.id.btnProductAdvanced).performClick();
                check(activity.findViewById(com.example.ecom.feature.admin.R.id.productAdvancedFields).getVisibility()==View.VISIBLE,"Product advanced fields did not expand");
            });
            runOnMainSync(() -> {
                activity.findViewById(com.example.ecom.feature.admin.R.id.btnSaveProduct).performClick();
                TextInputLayout name=activity.findViewById(com.example.ecom.feature.admin.R.id.inputNameLayout);
                check(name.getError()!=null,"Blank name must be rejected before network mutation");
            });
            back();
            if(!products.isEmpty()) {
                runOnMainSync(() -> AdminRouter.toAdminProductDetail(navHost(),products.get(0).id));
                waitUi(() -> text(com.example.ecom.feature.admin.R.id.textProductName,products.get(0).name),"Product detail did not load");
                screenshot("04-product-detail");
                runOnMainSync(() -> activity.findViewById(com.example.ecom.feature.admin.R.id.btnEditProduct).performClick());
                waitUi(() -> text(com.example.ecom.feature.admin.R.id.inputName,products.get(0).name),"Product edit did not load");
                back(); back();
            }
            tab(com.example.ecom.feature.admin.R.id.adminOrdersFragment);
            waitUi(() -> text(com.example.ecom.feature.admin.R.id.textResultCount,orders.size()+" kết quả"),"Order list did not load");
            screenshot("05-orders");
            runOnMainSync(() -> {
                activity.findViewById(com.example.ecom.feature.admin.R.id.btnToggleOrderFilters).performClick();
                check(activity.findViewById(com.example.ecom.feature.admin.R.id.advancedOrderFilters).getVisibility()==View.VISIBLE,"Advanced order filters did not expand");
            });
            screenshot("09-order-filters");
            if(!orders.isEmpty()) {
                runOnMainSync(() -> AdminRouter.toAdminOrderDetail(navHost(),orders.get(0).id));
                waitUi(() -> text(com.example.ecom.feature.admin.R.id.textOrderCode,"#DH"+orders.get(0).id),"Order detail did not load");
                back();
            }
            tab(com.example.ecom.feature.admin.R.id.adminUsersFragment);
            waitUi(() -> text(com.example.ecom.feature.admin.R.id.textResultCount,users.size()+" kết quả"),"User list did not load");
            screenshot("06-users");
            if(!users.isEmpty()) {
                runOnMainSync(() -> AdminRouter.toAdminUserDetail(navHost(),users.get(0).id));
                waitUi(() -> text(com.example.ecom.feature.admin.R.id.textCustomerEmail,users.get(0).email),"User detail did not load");
                screenshot("07-user-detail");
                back();
            }
            tab(com.example.ecom.feature.admin.R.id.adminRevenueFragment);
            waitUi(() -> { TextView v=activity.findViewById(com.example.ecom.feature.admin.R.id.revenueTotal); return v!=null && v.isShown() && !v.getText().toString().isEmpty(); },"Revenue did not load");
            screenshot("08-revenue");
            runOnMainSync(() -> AdminRouter.toAdminSettings(nav));
            waitUi(() -> { View v=activity.findViewById(com.example.ecom.feature.admin.R.id.inputShopName); return v!=null && v.isShown(); },"Settings did not load");
            screenshot("10-settings");
            back();
            tab(com.example.ecom.feature.admin.R.id.adminDashboardFragment);
            if(temporary!=null) {
                long temporaryId=temporary.id;
                this.<Void>request(cb -> api.deleteProduct(temporaryId,cb));
                List<AdminProduct> after=this.<List<AdminProduct>>request(api::products);
                for(AdminProduct product:after) check(product.id!=temporaryId,"DELETE did not remove temporary product");
                temporary=null;
            }
            if(screenshots) {
                waitUi(() -> { View v=activity.findViewById(com.example.ecom.feature.admin.R.id.metricProducts); return v!=null && v.isShown(); },"Dashboard did not finish loading");
                runOnMainSync(() -> ((com.example.ecom.feature.admin.fragments.AdminFragment)navHost()).refreshData());
                List<AdminProduct> current=this.<List<AdminProduct>>request(api::products);
                waitUi(() -> text(com.example.ecom.feature.admin.R.id.metricProducts,String.valueOf(current.size())),"Dashboard did not refresh after cleanup");
                screenshot("01-dashboard");
            }
            result.putString("stream","PASS: server-confirmed admin session restore; dashboard, lists/filters, product form validation, users/detail/stats, revenue/top/category stats. Products="+products.size()+", orders="+orders.size()+", users="+users.size()+". Product detail/edit "+(products.isEmpty()?"SKIPPED (empty)":"PASS")+"; order detail/transitions "+(orders.isEmpty()?"SKIPPED (empty)":"detail PASS, mutations not run")+". Product API CRUD "+(productCrud?"PASS; Base64 product/category preserve, clear, raw input and invalid/oversized rejection PASS; temporary records deleted":"not run")+". Password login not run (saved session).\n");
            finish(Activity.RESULT_OK,result);
        } catch(Throwable error) {
            if(temporary!=null) {
                long temporaryId=temporary.id;
                try { this.<Void>request(cb -> api.deleteProduct(temporaryId,cb)); }
                catch(Exception | AssertionError cleanup) { error.addSuppressed(cleanup); }
            }
            result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(error));
            finish(Activity.RESULT_CANCELED,result);
        }
    }
    private androidx.fragment.app.Fragment navHost() {
        return ((NavHostFragment)activity.getSupportFragmentManager().findFragmentById(com.example.ecom.feature.admin.R.id.adminNavHost))
            .getChildFragmentManager().getPrimaryNavigationFragment();
    }

    private String checkInlineImages() throws Exception {
        String png=null;
        for(boolean alpha:new boolean[]{true,false}) {
            android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(2200,1100,android.graphics.Bitmap.Config.ARGB_8888);
            bitmap.setHasAlpha(alpha); bitmap.eraseColor(alpha?0x80123456:0xff123456);
            java.io.File file=new java.io.File(getTargetContext().getCacheDir(),alpha?"qa-image.png":"qa-image.jpg");
            try {
                try(java.io.FileOutputStream output=new java.io.FileOutputStream(file)) {
                    check(bitmap.compress(alpha?android.graphics.Bitmap.CompressFormat.PNG:android.graphics.Bitmap.CompressFormat.JPEG,90,output),"Fixture encoding");
                } finally { bitmap.recycle(); }
                CountDownLatch done=new CountDownLatch(1);
                AtomicReference<String> value=new AtomicReference<>(),error=new AtomicReference<>();
                com.example.ecom.feature.admin.utils.AdminImageEncoder.encode(getTargetContext(),android.net.Uri.fromFile(file),(data,message) -> {
                    value.set(data); error.set(message); done.countDown();
                });
                check(done.await(20,TimeUnit.SECONDS),"Image encoder timeout");
                check(error.get()==null,"Image encoder: "+error.get());
                String encoded=value.get();
                check(encoded.startsWith(alpha?"data:image/png;base64,":"data:image/jpeg;base64,"),"Image MIME mapping");
                byte[] bytes=android.util.Base64.decode(encoded.substring(encoded.indexOf(',')+1),android.util.Base64.DEFAULT);
                check(bytes.length<=2*1024*1024,"Encoded image exceeds backend limit");
                android.graphics.BitmapFactory.Options options=new android.graphics.BitmapFactory.Options(); options.inJustDecodeBounds=true;
                android.graphics.BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
                check(options.outWidth>0 && options.outWidth<=1600 && options.outHeight<=1600,"Image was not resized");
                AtomicReference<android.widget.ImageView> target=new AtomicReference<>();
                runOnMainSync(() -> {
                    android.widget.ImageView view=new android.widget.ImageView(getTargetContext()); target.set(view);
                    com.example.ecom.core.ui.image.InlineImageLoader.load(view,encoded,com.example.ecom.feature.admin.R.drawable.admin_image_placeholder);
                });
                waitUi(() -> target.get().getDrawable() instanceof android.graphics.drawable.BitmapDrawable,"Base64 image did not display");
                if(alpha) png=encoded;
            } finally { check(file.delete(),"Temporary image cleanup"); }
        }
        AdminProduct product=new AdminProduct(); product.image=png;
        check(product.body().getString("image").equals(png),"Base64 body mapping");
        product.includeImage=false; check(!product.body().has("image"),"Unchanged image should be omitted");
        product.includeImage=true; product.image="";
        check(product.body().getString("image").isEmpty(),"Remove image should send empty string");
        return png;
    }

    private JSONObject raw(String method,String path,JSONObject body) throws Exception {
        return this.<JSONObject>request(cb -> com.example.ecom.core.api.ApiClient.getInstance(getTargetContext())
            .requestStrict(method,path,null,body,data -> (JSONObject)data,cb));
    }

    private void expectStatus(String method,String path,JSONObject body,int expected) throws Exception {
        CountDownLatch done=new CountDownLatch(1); AtomicInteger status=new AtomicInteger(-2);
        com.example.ecom.core.api.ApiClient.getInstance(getTargetContext()).requestStrict(method,path,null,body,data -> data,
            new ApiCallback<Object>() {
                public void onSuccess(Object ignored) { done.countDown(); }
                public void onError(String message,int code) { status.set(code); done.countDown(); }
            });
        check(done.await(45,TimeUnit.SECONDS),"Image validation timeout");
        check(status.get()==expected,"Expected image HTTP "+expected+", got "+status.get());
    }

    private void checkImageApiContract(AdminProduct product,String image) throws Exception {
        String path="/products/"+product.id;
        JSONObject body=product.body();
        for(String invalid:new String[]{"https://example.invalid/new.png","content://images/1","%%%",
                image.replace("data:image/png;","data:image/jpeg;")})
            expectStatus("PUT",path,body.put("image",invalid),400);
        String oversized=android.util.Base64.encodeToString(new byte[2*1024*1024+1],android.util.Base64.NO_WRAP);
        expectStatus("PUT",path,body.put("image",oversized),413);
        check(raw("PUT",path,body.put("image","")).isNull("image"),"Product image was not cleared");
        check(image.equals(raw("PUT",path,body.put("image",image.substring(image.indexOf(',')+1))).getString("image")),"Raw product Base64 not canonicalized");
        long categoryId=0;
        try {
            JSONObject category=raw("POST","/categories",new JSONObject().put("name","QA Base64 "+UUID.randomUUID()).put("image",image));
            categoryId=category.getLong("id");
            check(image.equals(category.getString("image")),"Category POST image");
            String categoryPath="/categories/"+categoryId;
            JSONObject edit=new JSONObject().put("name",category.getString("name"));
            check(image.equals(raw("PUT",categoryPath,edit).getString("image")),"Category omitted image not preserved");
            expectStatus("PUT",categoryPath,edit.put("image","https://example.invalid/new.png"),400);
            check(raw("PUT",categoryPath,edit.put("image","")).isNull("image"),"Category image was not cleared");
            check(image.equals(raw("PUT",categoryPath,edit.put("image",image.substring(image.indexOf(',')+1))).getString("image")),"Raw category Base64 not canonicalized");
            check(image.equals(raw("GET",categoryPath,null).getString("image")),"Category GET persisted Base64");
        } finally {
            if(categoryId!=0) raw("DELETE","/categories/"+categoryId,null);
        }
    }
}
