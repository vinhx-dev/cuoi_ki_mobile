package com.example.ecom.feature.product;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import com.example.ecom.core.data.*;
import com.example.ecom.core.navigation.*;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.core.ui.shop.*;
import com.example.ecom.core.ui.format.Formatter;
import com.google.android.material.button.MaterialButton;
public class ProductDetailFragment extends BaseFragment {
    public ProductDetailFragment() {
        super(R.layout.fragment_product_detail);
    }
    @Override public void onViewCreated(@NonNull View view,@Nullable Bundle state) {
        super.onViewCreated(view,state);
        android.content.Context c=requireContext();
        ProductDetailViewModel vm=viewModel(ProductDetailViewModel.class);
        LinearLayout host=view.findViewById(R.id.shopContent);
        ScrollView scroll=new ScrollView(c);
        scroll.setFillViewport(true);
        LinearLayout content=ShopViews.column(c);
        int pad=ShopViews.dp(c,16);
        content.setPadding(pad,pad,pad,pad);
        scroll.addView(content);
        host.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout actions=ShopViews.row(c);
        actions.setPadding(pad,ShopViews.dp(c,6),pad,ShopViews.dp(c,6));
        actions.setBackgroundColor(0xFFFFFFFF);
        MaterialButton cart=ShopViews.button(c,"");
        cart.setContentDescription("Thêm vào giỏ hàng");
        cart.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        cart.setIconResource(com.example.ecom.core.ui.R.drawable.shop_cart);
        cart.setTextColor(ShopViews.ORANGE);
        cart.setIconTint(android.content.res.ColorStateList.valueOf(ShopViews.ORANGE));
        cart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFF1E6));
        actions.addView(cart,new LinearLayout.LayoutParams(ShopViews.dp(c,80),-2));
        MaterialButton buy=ShopViews.button(c,"Mua ngay");
        LinearLayout.LayoutParams buyLp=new LinearLayout.LayoutParams(0,-2,1);
        buyLp.setMarginStart(ShopViews.dp(c,10));
        actions.addView(buy,buyLp);
        host.addView(actions);
        cart.setEnabled(false);
        buy.setEnabled(false);
        cart.setOnClickListener(v->openOptions(false));
        buy.setOnClickListener(v->openOptions(true));
        getChildFragmentManager().setFragmentResultListener("purchase",getViewLifecycleOwner(),(key,result)->vm.placeOrder(result.getInt("quantity"),result.getString("size"),apiCallback(id-> {
            toast("Đã tạo đơn #"+id);
            AppRouter.toOrderDetail(this,id);
        }
        )));
        vm.product.observe(getViewLifecycleOwner(),p-> {
            content.removeAllViews();
            content.addView(ShopViews.image(c,p,285));
            TextView price=ShopViews.heading(c,Formatter.money(p.price));
            price.setTextColor(ShopViews.ORANGE);
            content.addView(price);
            content.addView(ShopViews.text(c,p.name,24,true));
            TextView stock=ShopViews.text(c,p.stock>0?"Còn "+p.stock+" sản phẩm":"Tạm hết hàng",13,false);
            stock.setPadding(0,ShopViews.dp(c,8),0,0);
            stock.setTextColor(ShopViews.MUTED);
            content.addView(stock);
            TextView description=ShopViews.text(c,p.description,15,false);
            description.setLineSpacing(ShopViews.dp(c,4),1);
            description.setPadding(0,ShopViews.dp(c,12),0,0);
            content.addView(description);
            TextView note=ShopViews.text(c,p.isSample()?"Sản phẩm mẫu · Hình minh họa, chưa có ảnh thực tế. Không thể đặt mua sản phẩm mẫu.":"Hình minh họa · Ảnh sản phẩm thực tế cần được bổ sung.",12,false);
            note.setTextColor(ShopViews.MUTED);
            note.setPadding(0,ShopViews.dp(c,12),0,0);
            content.addView(note);
            content.addView(ShopViews.heading(c,"Sản phẩm tương tự"));
            LinearLayout related=ShopViews.row(c);
            int count=0;
            java.util.List<ShopProduct> candidates=new java.util.ArrayList<>();
            if(p.isSample()) {
                candidates.add(ShopCatalog.get(-5));
                candidates.add(ShopCatalog.get(-8));
            }
            else {
                /* Only real, previously loaded products may accompany real detail. */
                candidates.addAll(ShopCatalog.filter(ShopCatalog.real(),p.category,""));
            }
            for(ShopProduct other:candidates) {
                if(other==null||other.id==p.id||count==2)continue;
                View card=ShopViews.card(c,other,item->AppRouter.toProductDetail(this,item.id));
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
                lp.setMargins(ShopViews.dp(c,4),0,ShopViews.dp(c,4),0);
                related.addView(card,lp);
                count++;
            }
            content.addView(related);
            if(count==0)content.addView(ShopViews.text(c,"Chưa có sản phẩm tương tự",14,false));
            boolean enabled=p.stock>0&&!Boolean.TRUE.equals(vm.ordering.getValue());
            cart.setEnabled(enabled);
            buy.setEnabled(enabled);
        }
        );
        vm.ordering.observe(getViewLifecycleOwner(),busy-> {
            ShopProduct p=vm.product.getValue();
            cart.setEnabled(!busy&&p!=null&&p.stock>0);
            buy.setEnabled(!busy&&p!=null&&p.stock>0);
            buy.setText(busy?"Đang xử lý…":"Mua ngay");
        }
        );
        vm.error.observe(getViewLifecycleOwner(),error-> {
            if(error!=null&&!error.isEmpty()) {
                content.removeAllViews();
                content.addView(ShopViews.heading(c,"Không thể tải sản phẩm"));
                content.addView(ShopViews.text(c,error,14,false));
                MaterialButton retry=ShopViews.button(c,"Thử lại");
                retry.setOnClickListener(v->vm.load(requireArguments().getInt(AppPages.ARG_PRODUCT_ID)));
                content.addView(retry);
            }
        }
        );
        vm.load(requireArguments().getInt(AppPages.ARG_PRODUCT_ID));
    }
    private void openOptions(boolean buy) {
        if(getChildFragmentManager().findFragmentByTag("product_options")!=null)return;
        ShopProduct p=viewModel(ProductDetailViewModel.class).product.getValue();
        if(p!=null)ProductOptionBottomSheet.create(p.id,buy).show(getChildFragmentManager(),"product_options");
    }
}
