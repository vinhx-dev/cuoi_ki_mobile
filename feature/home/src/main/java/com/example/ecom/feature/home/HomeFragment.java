package com.example.ecom.feature.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecom.core.data.repository.AuthRepository;
import com.example.ecom.core.data.repository.ProductRepository;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;

/** Trang chủ: danh sách sản phẩm + tìm kiếm. */
public class HomeFragment extends BaseFragment {

    private ProductRepository productRepository;
    private ProductAdapter adapter;
    private EditText inputSearch;
    private TextView textEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        productRepository = new ProductRepository(requireContext());

        TextView textWelcome = view.findViewById(R.id.textViewWelcome);
        inputSearch = view.findViewById(R.id.inputSearch);
        textEmpty = view.findViewById(R.id.textEmpty);
        RecyclerView recycler = view.findViewById(R.id.recyclerProducts);

        textWelcome.setText(getString(R.string.welcome_user, new AuthRepository(requireContext()).getUserName()));

        adapter = new ProductAdapter(product -> AppRouter.toProductDetail(this, product.id));
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                loadProducts();
                return true;
            }
            return false;
        });

        loadProducts();
    }

    private void loadProducts() {
        productRepository.getProducts(inputSearch.getText().toString(), apiCallback(products -> {
            adapter.submit(products);
            textEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        }));
    }
}
