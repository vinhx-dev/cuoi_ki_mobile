package com.example.ecom.feature.admin.fragments;
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
    private EditText search;
    private AutoCompleteTextView filter;
    private int selected;
    private static final String[] STATUSES= {
        "PENDING","CONFIRMED","PROCESSING","SHIPPING","COMPLETED","CANCELLED"
    }
    ;
    @Override protected int layout() {
        return R.layout.fragment_admin_orders;
    }
    @Override protected void bind(View view) {
        search=view.findViewById(R.id.inputSearch);
        adapter=new AdminOrderAdapter(adminViewModel::customer,item->AdminRouter.toAdminOrderDetail(this,item.id));
        RecyclerView list=view.findViewById(R.id.adminList);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        filter=view.findViewById(R.id.inputFilter);
        List<String> choices=new ArrayList<>();
        choices.add(getString(R.string.admin_all_statuses));
        for(String status:STATUSES)choices.add(AdminUi.status(requireContext(),status));
        AdminUi.dropdown(filter,choices);
        filter.setText(choices.get(Math.min(selected,choices.size()-1)),false);
        filter.setOnItemClickListener((a,b,index,id)-> {
            selected=index;
            refresh();
        }
        );
        AdminUi.watch(search,this::refresh);
    }
    @Override protected void refresh() {
        if(root==null||adapter==null)return;
        String query=AdminUi.normalize(search.getText().toString());
        List<AdminOrder> result=new ArrayList<>();
        for(AdminOrder item:adminViewModel.getOrders())if((AdminUi.normalize(getString(R.string.admin_order_code,item.id)+" "+AdminUi.customerName(adminViewModel.customer(item.customerId))).contains(query))&&(selected==0||item.status.equals(STATUSES[selected-1])))result.add(item);
        adapter.submit(result);
        AdminUi.text(root,R.id.textResultCount,getString(R.string.admin_results,result.size()));
        root.findViewById(R.id.emptyState).setVisibility(result.isEmpty()?View.VISIBLE:View.GONE);
    }
    @Override public void onDestroyView() {
        adapter=null;
        search=null;
        filter=null;
        super.onDestroyView();
    }
}
