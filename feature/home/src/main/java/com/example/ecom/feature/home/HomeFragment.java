package com.example.ecom.feature.home;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecom.core.navigation.AppPages;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;

/** Trang chủ: danh sách sản phẩm, tìm kiếm bằng thanh search trên header. ≈ HomePage.jsx */
public class HomeFragment extends BaseFragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomeViewModel vm = viewModel(HomeViewModel.class);

        TextView textWelcome = view.findViewById(R.id.textViewWelcome);
        TextView textEmpty = view.findViewById(R.id.textEmpty);
        RecyclerView recycler = view.findViewById(R.id.recyclerProducts);

        textWelcome.setText(getString(R.string.welcome_user, vm.getUserName()));

        ProductAdapter adapter = new ProductAdapter(product -> AppRouter.toProductDetail(this, product.id));
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        // Render
        observe(vm.getProducts(), products -> {
            adapter.submit(products);
            textEmpty.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Lần đầu: lấy từ khoá được truyền sang (nếu có). Các lần sau: giữ từ khoá đang tìm.
        Bundle args = getArguments();
        String query = vm.getQuery() != null ? vm.getQuery()
                : args != null ? args.getString(AppPages.ARG_QUERY, "") : "";
        bindSearchBar(query, vm::search);
        vm.search(query);
    }
}
