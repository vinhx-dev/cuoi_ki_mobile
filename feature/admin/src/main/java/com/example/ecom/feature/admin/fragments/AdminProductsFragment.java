package com.example.ecom.feature.admin.fragments;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.AdminViewModel;
import com.example.ecom.feature.admin.adapters.*;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.data.*;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import java.util.*;

public class AdminProductsFragment extends AdminFragment {
    private AdminProductAdapter adapter;
    private EditText search;
    private AutoCompleteTextView filter;
    private long categoryId;
    private List<AdminProduct> products = new ArrayList<>();
    private List<AdminCategory> categories = new ArrayList<>();
    @Override protected int layout() { return R.layout.fragment_admin_products; }
    @Override protected void bind(View view) {
        search = view.findViewById(R.id.inputSearch);
        adapter = new AdminProductAdapter(item -> AdminRouter.toAdminProductDetail(this, item.id));
        RecyclerView list = view.findViewById(R.id.adminList);
        list.setLayoutManager(new LinearLayoutManager(requireContext())); list.setAdapter(adapter);
        filter = view.findViewById(R.id.inputFilter);
        filter.setOnItemClickListener((a,b,index,id) -> {
            categoryId = index == 0 ? 0 : categories.get(index - 1).id; render();
        });
        AdminUi.watch(search, this::render);
        view.findViewById(R.id.btnAddProduct).setOnClickListener(v -> AdminRouter.toAdminProductForm(this, 0));
    }
    @Override protected void refresh() {
        this.<AdminViewModel.Catalog>load(adminViewModel::catalog, data -> {
            products = data.products; categories = data.categories;
            List<String> choices = new ArrayList<>(); choices.add(getString(R.string.admin_all_categories));
            int selected = 0;
            for (AdminCategory c : categories) { choices.add(c.name); if (c.id == categoryId) selected = choices.size()-1; }
            if (selected == 0) categoryId = 0;
            AdminUi.dropdown(filter, choices); filter.setText(choices.get(selected), false); render();
        });
    }
    private void render() {
        if (root == null || adapter == null) return;
        String query = AdminUi.normalize(search.getText().toString());
        List<AdminProduct> result = new ArrayList<>();
        for (AdminProduct p : products)
            if (AdminUi.normalize(p.name + " SP" + p.id).contains(query) && (categoryId == 0 || p.categoryId == categoryId)) result.add(p);
        adapter.submit(result);
        AdminUi.text(root, R.id.textResultCount, getString(R.string.admin_results, result.size()));
        root.findViewById(R.id.emptyState).setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
