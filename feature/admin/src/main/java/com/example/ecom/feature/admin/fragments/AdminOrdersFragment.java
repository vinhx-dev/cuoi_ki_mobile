package com.example.ecom.feature.admin.fragments;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.adapters.*;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.data.*;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import java.util.*;

public class AdminOrdersFragment extends AdminFragment {
    private AdminOrderAdapter adapter;
    private EditText search, userFilter;
    private int selected;
    private List<AdminOrder> orders = new ArrayList<>();
    private static final String[] STATUSES = {"PENDING","SHIPPING","DELIVERED","CANCELLED"};
    @Override protected int layout() { return R.layout.fragment_admin_orders; }
    @Override protected void bind(View view) {
        search = view.findViewById(R.id.inputSearch);
        userFilter = view.findViewById(R.id.inputUserId);
        adapter = new AdminOrderAdapter(item -> AdminRouter.toAdminOrderDetail(this, item.id));
        RecyclerView list = view.findViewById(R.id.adminList);
        list.setLayoutManager(new LinearLayoutManager(requireContext())); list.setAdapter(adapter);
        AutoCompleteTextView filter = view.findViewById(R.id.inputFilter);
        List<String> choices = new ArrayList<>(); choices.add(getString(R.string.admin_all_statuses));
        for (String status : STATUSES) choices.add(AdminUi.status(requireContext(), status));
        AdminUi.dropdown(filter, choices); filter.setText(choices.get(selected), false);
        filter.setOnItemClickListener((a,b,index,id) -> { selected=index; refresh(); });
        AdminUi.watch(search, this::render);
        view.findViewById(R.id.btnFilterOrders).setOnClickListener(v -> refresh());
        view.findViewById(R.id.btnToggleOrderFilters).setOnClickListener(v -> {
            View filters=root.findViewById(R.id.advancedOrderFilters);
            filters.setVisibility(filters.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
        });
    }
    @Override protected void refresh() {
        Long userId = null;
        String text = userFilter.getText().toString().trim();
        if (!text.isEmpty()) {
            try { userId = Long.parseLong(text); if (userId <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException e) { userFilter.setError(getString(R.string.admin_invalid_user_id)); return; }
        }
        final Long id = userId;
        this.<List<AdminOrder>>load(cb -> adminViewModel.orders(selected==0?null:STATUSES[selected-1], id, cb),
                data -> { orders=data; render(); });
    }
    private void render() {
        if (root == null) return;
        String query = AdminUi.normalize(search.getText().toString());
        List<AdminOrder> result = new ArrayList<>();
        for (AdminOrder order : orders)
            if (AdminUi.normalize(getString(R.string.admin_order_code, order.id) + " " + order.userName).contains(query)) result.add(order);
        adapter.submit(result);
        AdminUi.text(root, R.id.textResultCount, getString(R.string.admin_results,result.size()));
        root.findViewById(R.id.emptyState).setVisibility(result.isEmpty()?View.VISIBLE:View.GONE);
    }
}
