package com.example.ecom.feature.admin.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.feature.admin.AdminViewModel;
import com.example.ecom.feature.admin.activities.AdminActivity;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.AdminUi;
import com.google.android.material.button.MaterialButton;
import java.util.function.Consumer;

/** Lifecycle-scoped loading, error and retry within the admin design system. */
public abstract class AdminFragment extends BaseFragment {
    protected AdminViewModel adminViewModel;
    protected View root;
    private LinearLayout banner;
    private TextView stateText;
    private ProgressBar progress;
    private MaterialButton retry;
    private int generation;
    private boolean loading, loaded, actionInFlight;
    protected AdminFragment() { super(0); }
    protected abstract int layout();
    protected abstract void bind(View view);
    protected void refresh() {}
    public final void refreshData() {
        if(root==null) return;
        if(loading || actionInFlight) { toast(getString(R.string.admin_busy)); return; }
        refresh();
    }
    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        adminViewModel=new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup parent,@Nullable Bundle state) {
        android.content.Context context=themedContext();
        LayoutInflater themed=inflater.cloneInContext(context);
        LinearLayout wrapper=new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setBackgroundResource(R.color.admin_background_secondary);
        banner=new LinearLayout(context); banner.setGravity(Gravity.CENTER_VERTICAL);
        int pad=AdminUi.dp(context,12); banner.setPadding(pad,0,pad,0);
        banner.setBackgroundResource(R.drawable.admin_avatar_background);
        progress=new ProgressBar(context);
        banner.addView(progress,new LinearLayout.LayoutParams(AdminUi.dp(context,20),AdminUi.dp(context,20)));
        stateText=new TextView(context); stateText.setTextSize(12);
        stateText.setPadding(pad,pad,pad,pad);
        stateText.setTextColor(androidx.core.content.ContextCompat.getColor(context,R.color.admin_text));
        stateText.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        banner.addView(stateText,new LinearLayout.LayoutParams(0,-2,1));
        retry=new MaterialButton(context);
        retry.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        retry.setTextColor(androidx.core.content.ContextCompat.getColor(context,R.color.admin_primary));
        retry.setText(R.string.admin_retry); retry.setTextSize(12); retry.setMinWidth(0);
        retry.setMinimumWidth(0); retry.setPadding(pad,0,pad,0);
        retry.setOnClickListener(v -> refreshData());
        banner.addView(retry,new LinearLayout.LayoutParams(-2,AdminUi.dp(context,48)));
        LinearLayout.LayoutParams bannerParams=new LinearLayout.LayoutParams(-1,-2);
        bannerParams.setMargins(AdminUi.dp(context,20),AdminUi.dp(context,6),AdminUi.dp(context,20),AdminUi.dp(context,8));
        wrapper.addView(banner,bannerParams); banner.setVisibility(View.GONE);
        root=themed.inflate(layout(),wrapper,false);
        wrapper.addView(root,new LinearLayout.LayoutParams(-1,0,1));
        return wrapper;
    }
    @Override public void onViewCreated(@NonNull View view,@Nullable Bundle state) {
        super.onViewCreated(view,state); bind(root);
    }
    @Override public void onResume() { super.onResume(); if(root!=null) refresh(); }
    private void pending(int message) {
        banner.setVisibility(View.VISIBLE); progress.setVisibility(View.VISIBLE); retry.setVisibility(View.GONE);
        banner.setBackgroundResource(R.drawable.admin_avatar_background); stateText.setText(message);
    }
    private void failed(String message,int status) {
        progress.setVisibility(View.GONE); retry.setVisibility(View.VISIBLE);
        banner.setVisibility(View.VISIBLE); banner.setBackgroundResource(R.drawable.admin_error_surface);
        stateText.setText(errorMessage(message,status));
    }
    protected <T> void load(Consumer<ApiCallback<T>> request,Consumer<T> render) {
        final View view=root; final int current=++generation; loading=true;
        if(!loaded) root.setVisibility(View.GONE);
        pending(R.string.admin_loading);
        request.accept(new ApiCallback<T>() {
            private boolean active() { return root==view && generation==current && isAdded(); }
            public void onSuccess(T data) {
                if(!active()) return;
                loading=false; loaded=true; banner.setVisibility(View.GONE);
                root.setVisibility(View.VISIBLE); render.accept(data);
            }
            public void onError(String message,int status) {
                if(!active()) return;
                loading=false; failed(message,status); root.setVisibility(View.GONE);
            }
        });
    }
    protected <T> void action(String key,Consumer<ApiCallback<T>> request,Consumer<T> success,int... buttons) {
        if(loading || actionInFlight || adminViewModel.busy(key)) { toast(getString(R.string.admin_busy)); return; }
        generation++; actionInFlight=true; View view=root;
        for(int id:buttons) root.findViewById(id).setEnabled(false);
        pending(R.string.admin_saving);
        adminViewModel.mutate(key,request,new ApiCallback<T>() {
            public void onSuccess(T data) {
                if(root!=view || !isAdded()) return;
                finish(); success.accept(data);
            }
            public void onError(String message,int status) {
                if(root!=view || !isAdded()) return;
                finish(); failed(message,status); toast(errorMessage(message,status));
            }
            private void finish() {
                actionInFlight=false; banner.setVisibility(View.GONE);
                for(int id:buttons) root.findViewById(id).setEnabled(true);
            }
        });
    }
    private String errorMessage(String message,int status) {
        if(status==401) return getString(R.string.admin_session_expired)+"\n"+message;
        if(status==403) return getString(R.string.admin_forbidden)+"\n"+message;
        return message;
    }
    @Override public void onDestroyView() {
        generation++; loading=false; loaded=false; actionInFlight=false;
        root=null; stateText=null; progress=null; retry=null; banner=null;
        super.onDestroyView();
    }
    protected android.content.Context themedContext() {
        return new ContextThemeWrapper(requireContext(),R.style.ThemeOverlay_ShopAdmin);
    }
    protected void setTitle(CharSequence title) { ((AdminActivity)requireActivity()).setHeaderTitle(title); }
}
