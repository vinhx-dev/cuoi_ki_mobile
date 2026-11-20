package com.example.ecom.feature.admin.fragments;
import android.view.View;
import android.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.data.AdminProduct;
import com.example.ecom.feature.admin.navigation.AdminRouter;

public class AdminProductDetailFragment extends AdminFragment {
    private long id;
    private AdminProduct product;
    @Override protected int layout() { return R.layout.fragment_admin_product_detail; }
    @Override protected void bind(View view) {
        id=requireArguments().getLong(AdminRouter.ARG_ADMIN_PRODUCT_ID);
        view.findViewById(R.id.btnEditProduct).setOnClickListener(v -> { if (product!=null) AdminRouter.toAdminProductForm(this,id); });
        view.findViewById(R.id.btnDeleteProduct).setOnClickListener(v -> {
            if(product==null || adminViewModel.busy("product:"+id)) return;
            new MaterialAlertDialogBuilder(themedContext()).setTitle(R.string.admin_delete_title)
                .setMessage(getString(R.string.admin_delete_message,product.name)).setNegativeButton(R.string.admin_cancel,null)
                .setPositiveButton(R.string.admin_delete,(d,which) -> this.<Void>action("product:"+id,
                    cb -> adminViewModel.deleteProduct(id,cb), ignored -> {
                        toast(getString(R.string.admin_deleted)); AdminRouter.back(this);
                    },R.id.btnEditProduct,R.id.btnDeleteProduct)).show();
        });
    }
    @Override protected void refresh() {
        this.<AdminProduct>load(cb -> adminViewModel.product(id,cb), p -> {
            product=p;
            AdminUi.text(root,R.id.textProductName,p.name);
            AdminUi.text(root,R.id.textProductCode,getString(R.string.admin_product_code,p.id));
            AdminUi.text(root,R.id.textProductPrice,AdminUi.money(p.price));
            AdminUi.text(root,R.id.textProductStock,getString(R.string.admin_stock_count,p.stock));
            AdminUi.text(root,R.id.textProductCategory,p.category.isEmpty()?getString(R.string.admin_category_unset):p.category);
            AdminUi.text(root,R.id.textProductDescription,p.description);
            AdminUi.text(root,R.id.textProductSizes,getString(R.string.admin_sizes)+": "+android.text.TextUtils.join(", ",p.sizes));
            AdminUi.text(root,R.id.textProductColors,getString(R.string.admin_sold_count,p.soldCount));
            LinearLayout images=root.findViewById(R.id.productImages); images.removeAllViews();
            ImageView image=new ImageView(themedContext());
            image.setLayoutParams(new LinearLayout.LayoutParams(-1,AdminUi.dp(requireContext(),240)));
            image.setPadding(24,24,24,24);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER); image.setContentDescription(p.name);
            AdminImageLoader.load(image,p.image,R.drawable.admin_image_placeholder); images.addView(image);
        });
    }
}
