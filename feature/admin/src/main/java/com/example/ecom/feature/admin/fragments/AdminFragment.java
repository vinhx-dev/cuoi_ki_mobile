package com.example.ecom.feature.admin.fragments;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.view.ContextThemeWrapper;
import com.example.ecom.core.ui.base.BaseFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecom.feature.admin.AdminViewModel;
import com.example.ecom.feature.admin.activities.AdminActivity;
import com.example.ecom.feature.admin.R;
public abstract class AdminFragment extends BaseFragment {
    protected AdminViewModel adminViewModel;
    protected AdminFragment() {
        // AdminActivity supplies the shell; this fragment inflates its own themed content.
        super(0);
    }
    protected View root;
    protected abstract int layout();
    protected abstract void bind(View view);
    protected void refresh() {
    }
    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        adminViewModel=new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
    }
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup parent,@Nullable Bundle state) {
        return inflater.cloneInContext(new ContextThemeWrapper(requireContext(),R.style.ThemeOverlay_ShopAdmin)).inflate(layout(),parent,false);
    }
    @Override public void onViewCreated(@NonNull View view,@Nullable Bundle state) {
        super.onViewCreated(view,state);
        root=view;
        bind(view);
    }
    @Override public void onResume() {
        super.onResume();
        if(root!=null)refresh();
    }
    @Override public void onDestroyView() {
        root=null;
        super.onDestroyView();
    }
    protected android.content.Context themedContext() {
        return new ContextThemeWrapper(requireContext(),R.style.ThemeOverlay_ShopAdmin);
    }
    protected void setTitle(CharSequence title) {
        ((AdminActivity)requireActivity()).setHeaderTitle(title);
    }
}
