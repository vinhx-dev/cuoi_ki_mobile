package com.example.ecom.feature.admin.adapters;
import android.view.View;
import android.widget.ImageView;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.AdminUi;
import com.example.ecom.feature.admin.data.AdminProduct;
import java.util.function.Consumer;
public class AdminProductAdapter extends AdminListAdapter<AdminProduct> {
    public AdminProductAdapter(Consumer<AdminProduct> click) {
        super(R.layout.item_admin_product,click);
    }
    @Override protected void bind(View v,AdminProduct p) {
        AdminUi.text(v,R.id.itemName,p.name);
        AdminUi.text(v,R.id.itemPrice,AdminUi.money(p.price));
        AdminUi.text(v,R.id.itemCategory,p.category.isEmpty()?v.getContext().getString(R.string.admin_category_unset):p.category);
        AdminUi.badge(v.findViewById(R.id.itemStock),v.getContext().getString(p.stock==0?R.string.admin_stock_empty:R.string.admin_stock_remaining,p.stock),
            p.stock==0?R.color.admin_danger:p.stock<=5?R.color.admin_warning:R.color.admin_success,
            p.stock==0?R.color.admin_danger_light:p.stock<=5?R.color.admin_warning_light:R.color.admin_success_light);
        AdminUi.productImage((ImageView)v.findViewById(R.id.itemImage),p);
    }
}
