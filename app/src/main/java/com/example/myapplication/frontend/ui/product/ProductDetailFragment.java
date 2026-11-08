package com.example.myapplication.frontend.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.myapplication.frontend.navigation.AppRouter;
import com.example.myapplication.frontend.ui.base.BaseFragment;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.frontend.data.model.Product;
import com.example.myapplication.frontend.data.repository.OrderRepository;
import com.example.myapplication.frontend.data.repository.ProductRepository;
import com.example.myapplication.frontend.utils.Formatter;

public class ProductDetailFragment extends BaseFragment {

    private ProductRepository productRepository;
    private OrderRepository orderRepository;

    private TextView textName, textPrice, textStock, textDescription, textQuantity;
    private Button btnOrder;

    private Product product;
    private int quantity = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productRepository = new ProductRepository(requireContext());
        orderRepository = new OrderRepository(requireContext());

        textName = view.findViewById(R.id.textName);
        textPrice = view.findViewById(R.id.textPrice);
        textStock = view.findViewById(R.id.textStock);
        textDescription = view.findViewById(R.id.textDescription);
        textQuantity = view.findViewById(R.id.textQuantity);
        btnOrder = view.findViewById(R.id.btnOrder);

        view.findViewById(R.id.btnMinus).setOnClickListener(v -> changeQuantity(-1));
        view.findViewById(R.id.btnPlus).setOnClickListener(v -> changeQuantity(+1));
        btnOrder.setOnClickListener(v -> placeOrder());

        int productId = requireArguments().getInt(AppRouter.ARG_PRODUCT_ID);
        productRepository.getProduct(productId, apiCallback(this::bind));
    }

    private void bind(Product p) {
        product = p;
        setTitle(p.name);
        textName.setText(p.name);
        textPrice.setText(Formatter.money(p.price));
        textStock.setText("Kho: " + p.stock);
        textDescription.setText(p.description);
        quantity = Math.min(quantity, Math.max(p.stock, 1));
        textQuantity.setText(String.valueOf(quantity));
        btnOrder.setEnabled(p.stock > 0);
    }

    private void changeQuantity(int delta) {
        if (product == null) return;
        quantity = Math.max(1, Math.min(product.stock, quantity + delta));
        textQuantity.setText(String.valueOf(quantity));
    }

    private void placeOrder() {
        if (product == null) return;
        orderRepository.createOrder(product.id, quantity, null, apiCallback(order -> {
            toast("Đặt hàng thành công #" + order.id);
            AppRouter.toOrderDetail(this, order.id);
        }));
    }
}
