package com.example.ecom.feature.home;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.recyclerview.widget.*;
import com.example.ecom.core.data.*;
import com.example.ecom.core.navigation.*;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.core.ui.shop.*;
/** Home and category results share one catalogue and the same reusable product grid. */
public class HomeFragment extends BaseFragment {
    public HomeFragment() {
        super(R.layout.fragment_home);
    }
    @Override public void onViewCreated(@NonNull View view,@Nullable Bundle state) {
        super.onViewCreated(view,state);
        android.content.Context c=requireContext();
        HomeViewModel vm=viewModel(HomeViewModel.class);
        vm.category=requireArguments().getString("category","");
        if(state!=null)vm.query=state.getString("search",vm.query);
        else if(vm.query.isEmpty())vm.query=requireArguments().getString(AppPages.ARG_QUERY,"");
        LinearLayout host=view.findViewById(R.id.shopContent);
        RecyclerView grid=new RecyclerView(c);
        grid.setClipToPadding(false);
        grid.setPadding(ShopViews.dp(c,11),0,ShopViews.dp(c,11),ShopViews.dp(c,16));
        host.addView(grid,new LinearLayout.LayoutParams(-1,-1));
        LinearLayout header=ShopViews.column(c);
        header.setPadding(ShopViews.dp(c,5),0,ShopViews.dp(c,5),0);
        if(vm.category.isEmpty()) {
            header.addView(ShopViews.heading(c,"Danh mục"));
            GridLayout categories=new GridLayout(c);
            categories.setColumnCount(4);
            vm.categories.observe(getViewLifecycleOwner(), names -> {
                categories.removeAllViews();
                for (String category : names) {
                    LinearLayout item=ShopViews.column(c);
                    item.setGravity(Gravity.CENTER);
                    item.setPadding(0,0,0,ShopViews.dp(c,14));
                    ImageView icon=new ImageView(c);
                    icon.setImageResource(ShopViews.icon(category));
                    icon.setPadding(ShopViews.dp(c,12),ShopViews.dp(c,12),ShopViews.dp(c,12),ShopViews.dp(c,12));
                    icon.setBackground(ShopViews.background(0xFFFFF1E6,28,c));
                    item.addView(icon,new LinearLayout.LayoutParams(ShopViews.dp(c,54),ShopViews.dp(c,54)));
                    TextView label=ShopViews.text(c,category,12,false);
                    label.setPadding(0,ShopViews.dp(c,6),0,0);
                    label.setGravity(Gravity.CENTER);
                    item.addView(label,new LinearLayout.LayoutParams(-1,-2));
                    GridLayout.LayoutParams lp=new GridLayout.LayoutParams();
                    lp.width=0;
                    lp.setGravity(Gravity.FILL_HORIZONTAL);
                    lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
                    item.setLayoutParams(lp);
                    item.setOnClickListener(v->AppRouter.toProductList(this,category));
                    item.setFocusable(true);
                    item.setContentDescription("Xem danh mục "+category);
                    categories.addView(item);
                }
            });
            header.addView(categories);
            header.addView(ShopViews.heading(c,"Gợi ý cho bạn"));
        }
        else {
            view.findViewById(com.example.ecom.core.ui.R.id.headerBack).setVisibility(View.VISIBLE);
            header.addView(ShopViews.heading(c,"Sản phẩm: "+vm.category));
        }
        TextView note=ShopViews.text(c,"",12,false);
        note.setTextColor(ShopViews.MUTED);
        note.setPadding(0,0,0,ShopViews.dp(c,12));
        header.addView(note);
        note.setOnClickListener(v->vm.refresh());
        TextView empty=ShopViews.text(c,"",14,false);
        header.addView(empty);
        RecyclerView.Adapter<RecyclerView.ViewHolder> headerAdapter=new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p,int t) {
                return new RecyclerView.ViewHolder(header) {
                }
                ;
            }
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h,int p) {
            }
            public int getItemCount() {
                return 1;
            }
        }
        ;
        ProductGridAdapter products=new ProductGridAdapter(p->AppRouter.toProductDetail(this,p.id));
        ConcatAdapter all=new ConcatAdapter(headerAdapter,products);
        GridLayoutManager manager=new GridLayoutManager(c,2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            public int getSpanSize(int p) {
                return p==0?2:1;
            }
        }
        );
        grid.setLayoutManager(manager);
        grid.setAdapter(all);
        vm.products.observe(getViewLifecycleOwner(),list-> {
            products.submit(list);
            empty.setText(list.isEmpty()&&!"Đang tải sản phẩm…".equals(vm.message.getValue())?"Chưa có sản phẩm phù hợp. Hãy thử từ khóa hoặc danh mục khác.":"");
        }
        );
        vm.message.observe(getViewLifecycleOwner(),note::setText);
        bindSearchBar(vm.query,vm::search);
        vm.load();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("search",viewModel(HomeViewModel.class).query);
    }
}
