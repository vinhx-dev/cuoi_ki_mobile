package com.example.ecom.feature.admin.adapters;
import android.view.View;
import android.widget.ImageView;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.AdminUi;
import com.example.ecom.core.data.model.admin.AdminProduct;
import java.util.function.Consumer;
public class AdminProductAdapter extends AdminListAdapter<AdminProduct> {
    public AdminProductAdapter(Consumer<AdminProduct> click) {
        super(R.layout.item_admin_product,click);
    }
    @Override protected void bind(View v,AdminProduct p) {
        AdminUi.text(v,R.id.itemName,p.name);
        AdminUi.text(v,R.id.itemPrice,AdminUi.money(p.price));
        AdminUi.text(v,R.id.itemStock,v.getContext().getString(R.string.admin_stock_count,p.stock));
        AdminUi.productImage((ImageView)v.findViewById(R.id.itemImage),p);
    }
}
