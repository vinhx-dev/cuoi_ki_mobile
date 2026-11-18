package com.example.ecom.feature.admin.fragments;
import android.view.View;
import android.widget.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.data.AdminProduct;
import com.example.ecom.feature.admin.navigation.AdminRouter;
public class AdminProductDetailFragment extends AdminFragment {
    private int id;
    @Override protected int layout() {
        return R.layout.fragment_admin_product_detail;
    }
    @Override protected void bind(View view) {
        id=requireArguments().getInt(AdminRouter.ARG_ADMIN_PRODUCT_ID);
        view.findViewById(R.id.btnEditProduct).setOnClickListener(v->AdminRouter.toAdminProductForm(this,id));
        view.findViewById(R.id.btnDeleteProduct).setOnClickListener(v-> {
            AdminProduct p=adminViewModel.product(id);
            if(p==null)return;
            new MaterialAlertDialogBuilder(themedContext()).setTitle(R.string.admin_delete_title).setMessage(getString(R.string.admin_delete_message,p.name)).setNegativeButton(R.string.admin_cancel,null).setPositiveButton(R.string.admin_delete,(d,which)-> {
                adminViewModel.deleteProduct(id);
                toast(getString(R.string.admin_deleted));
                AdminRouter.back(this);
            }
            ).show();
        }
        );
    }
    @Override protected void refresh() {
        AdminProduct p=adminViewModel.product(id);
        if(p==null) {
            toast(getString(R.string.admin_not_found));
            AdminRouter.back(this);
            return;
        }
        AdminUi.text(root,R.id.textProductName,p.name);
        AdminUi.text(root,R.id.textProductCode,getString(R.string.admin_product_code,p.id));
        AdminUi.text(root,R.id.textProductPrice,AdminUi.money(p.price));
        AdminUi.text(root,R.id.textProductStock,getString(R.string.admin_stock_count,p.stock));
        AdminUi.text(root,R.id.textProductCategory,p.category);
        AdminUi.text(root,R.id.textProductDescription,p.description);
        AdminUi.text(root,R.id.textProductSizes,getString(R.string.admin_sizes)+": "+p.sizes);
        AdminUi.text(root,R.id.textProductColors,getString(R.string.admin_colors)+": "+p.colors);
        LinearLayout images=root.findViewById(R.id.productImages);
        images.removeAllViews();
        int count=Math.max(1,p.images.size());
        for(int i=0;i<count;i++) {
            ImageView image=new ImageView(themedContext());
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(AdminUi.dp(requireContext(),220),AdminUi.dp(requireContext(),245));
            params.setMarginEnd(AdminUi.dp(requireContext(),12));
            image.setLayoutParams(params);
            image.setBackgroundResource(R.drawable.admin_image_background);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setContentDescription(getString(R.string.admin_image_preview,i+1));
            image.setImageResource(AdminUi.image(p.imageKey));
            if(!p.images.isEmpty())AdminImageLoader.load(image,p.images.get(i),AdminUi.image(p.imageKey));
            images.addView(image);
        }
    }
}
