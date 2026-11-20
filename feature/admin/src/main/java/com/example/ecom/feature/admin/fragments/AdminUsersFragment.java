package com.example.ecom.feature.admin.fragments;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.adapters.*;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.data.*;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import java.util.*;

public class AdminUsersFragment extends AdminFragment {
    private AdminCustomerAdapter adapter;
    private EditText search;
    private int selected;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable searchRequest = () -> { if (root != null) refresh(); };
    private static final String[] STATUSES = {"ACTIVE","PENDING","BLOCKED"};
    @Override protected int layout() { return R.layout.fragment_admin_users; }
    @Override protected void bind(View view) {
        search = view.findViewById(R.id.inputSearch);
        adapter = new AdminCustomerAdapter(item -> AdminRouter.toAdminUserDetail(this,item.id));
        RecyclerView list = view.findViewById(R.id.adminList);
        list.setLayoutManager(new LinearLayoutManager(requireContext())); list.setAdapter(adapter);
        AutoCompleteTextView filter = view.findViewById(R.id.inputUserStatus);
        List<String> choices = new ArrayList<>(); choices.add(getString(R.string.admin_all_statuses));
        for (String status : STATUSES) choices.add(AdminUi.userStatus(requireContext(), status));
        AdminUi.dropdown(filter, choices); filter.setText(choices.get(selected), false);
        filter.setOnItemClickListener((a,b,index,id) -> { selected=index; refresh(); });
        AdminUi.watch(search, () -> { handler.removeCallbacks(searchRequest); handler.postDelayed(searchRequest, 400); });
    }
    @Override protected void refresh() {
        handler.removeCallbacks(searchRequest);
        this.<List<AdminCustomer>>load(cb -> adminViewModel.users(search.getText().toString().trim(),
                selected==0?null:STATUSES[selected-1], cb), result -> {
            adapter.submit(result);
            AdminUi.text(root,R.id.textResultCount,getString(R.string.admin_results,result.size()));
            root.findViewById(R.id.emptyState).setVisibility(result.isEmpty()?View.VISIBLE:View.GONE);
        });
    }
    @Override public void onDestroyView() { handler.removeCallbacks(searchRequest); super.onDestroyView(); }
}
