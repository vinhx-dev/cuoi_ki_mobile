package com.example.ecom.feature.admin.fragments;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.appcompat.view.ContextThemeWrapper;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.core.data.repository.AdminRepository;
import com.example.ecom.feature.admin.R;
public abstract class AdminFragment extends BaseFragment {
    protected AdminRepository repository;
    protected View root;
    protected abstract int layout();
    protected abstract void bind(View view);
    protected void refresh() {
    }
    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        repository=AdminRepository.getInstance(requireContext());
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
}
