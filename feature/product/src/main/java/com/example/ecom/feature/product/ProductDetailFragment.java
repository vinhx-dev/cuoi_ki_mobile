package com.example.ecom.feature.product;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.navigation.AppPages;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.core.ui.format.Formatter;

/** Chi tiết sản phẩm + đặt hàng nhanh. ≈ ProductDetailPage.jsx */
public class ProductDetailFragment extends BaseFragment {

    public ProductDetailFragment() {
        super(R.layout.fragment_product_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProductDetailViewModel vm = viewModel(ProductDetailViewModel.class);

        TextView textName = view.findViewById(R.id.textName);
        TextView textPrice = view.findViewById(R.id.textPrice);
        TextView textStock = view.findViewById(R.id.textStock);
        TextView textDescription = view.findViewById(R.id.textDescription);
        TextView textQuantity = view.findViewById(R.id.textQuantity);
        Button btnOrder = view.findViewById(R.id.btnOrder);

        // Sự kiện
        view.findViewById(R.id.btnMinus).setOnClickListener(v -> vm.changeQuantity(-1));
        view.findViewById(R.id.btnPlus).setOnClickListener(v -> vm.changeQuantity(+1));
        btnOrder.setOnClickListener(v -> vm.placeOrder(apiCallback(orderId -> {
            toast(getString(R.string.msg_order_success, orderId));
            AppRouter.toOrderDetail(this, orderId);
        })));

        // Render
        observe(vm.getProduct(), p -> {
            textName.setText(p.name);
            textPrice.setText(Formatter.money(p.price));
            textStock.setText(getString(R.string.product_stock, p.stock));
            textDescription.setText(p.description);
            btnOrder.setEnabled(p.stock > 0);
        });
        vm.getQuantity().observe(getViewLifecycleOwner(), q -> textQuantity.setText(String.valueOf(q)));

        vm.load(requireArguments().getInt(AppPages.ARG_PRODUCT_ID));
    }
}
