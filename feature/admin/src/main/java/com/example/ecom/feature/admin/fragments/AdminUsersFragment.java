package com.example.ecom.feature.admin.fragments;
import android.view.View;
import android.widget.*;
import androidx.recyclerview.widget.*;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.adapters.*;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.core.data.model.admin.*;
import com.example.ecom.core.navigation.AppRouter;
import java.util.*;
public class AdminUsersFragment extends AdminFragment {
    private AdminCustomerAdapter adapter;
    private EditText search;
    @Override protected int layout() {
        return R.layout.fragment_admin_users;
    }
    @Override protected void bind(View view) {
        search=view.findViewById(R.id.inputSearch);
        adapter=new AdminCustomerAdapter(repository,item->AppRouter.toAdminUserDetail(this,item.id));
        RecyclerView list=view.findViewById(R.id.adminList);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        AdminUi.watch(search,this::refresh);
    }
    @Override protected void refresh() {
        if(root==null||adapter==null)return;
        String query=AdminUi.normalize(search.getText().toString());
        List<AdminCustomer> result=new ArrayList<>();
        for(AdminCustomer item:repository.getCustomers())if(AdminUi.normalize(item.name+" "+item.email+" "+item.phone).contains(query))result.add(item);
        adapter.submit(result);
        AdminUi.text(root,R.id.textResultCount,getString(R.string.admin_results,result.size()));
        root.findViewById(R.id.emptyState).setVisibility(result.isEmpty()?View.VISIBLE:View.GONE);
    }
    @Override public void onDestroyView() {
        adapter=null;
        search=null;
        super.onDestroyView();
    }
}
