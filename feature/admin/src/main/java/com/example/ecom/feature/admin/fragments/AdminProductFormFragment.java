package com.example.ecom.feature.admin.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.AdminViewModel;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.data.*;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import com.google.gson.*;
import java.math.BigDecimal;
import java.util.*;

public class AdminProductFormFragment extends AdminFragment {
    private long id, categoryId;
    private boolean initialized, leaving;
    private String original = "";
    private Bundle draft;
    private AdminProduct source;
    private List<AdminCategory> categories = new ArrayList<>();
    private static final int[] INPUTS = {R.id.inputNameLayout,R.id.inputPriceLayout,R.id.inputStockLayout,
        R.id.inputDescriptionLayout,R.id.inputImageLayout,R.id.inputSizesLayout,R.id.inputSoldCountLayout,R.id.inputForAiLayout};

    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        if(state!=null) { draft=state.getBundle("draft"); original=state.getString("original",""); }
    }
    @Override protected int layout() { return R.layout.fragment_admin_product_form; }
    @Override protected void bind(View view) {
        id=requireArguments().getLong(AdminRouter.ARG_ADMIN_PRODUCT_ID,0);
        setTitle(getString(id==0?R.string.admin_title_product_add:R.string.admin_title_product_edit));
        AdminUi.text(view,R.id.textFormHeading,getString(id==0?R.string.admin_title_product_add:R.string.admin_title_product_edit));
        view.findViewById(R.id.btnProductAdvanced).setOnClickListener(v -> {
            View advanced=root.findViewById(R.id.productAdvancedFields);
            advanced.setVisibility(advanced.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
        });
        AutoCompleteTextView category=view.findViewById(R.id.inputCategory);
        category.setOnItemClickListener((a,b,index,row) -> { categoryId=index==0?0:categories.get(index-1).id; });
        for(int inputId:INPUTS) {
            TextInputLayout input=view.findViewById(inputId);
            AdminUi.watch(input.getEditText(),() -> input.setError(null));
        }
        view.findViewById(R.id.btnAddImages).setOnClickListener(v -> preview());
        view.findViewById(R.id.btnSaveProduct).setOnClickListener(v -> save());
        view.findViewById(R.id.btnCancelProduct).setOnClickListener(v -> requestLeave());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { requestLeave(); }
        });
        // Do not accept edits until the current DTO and category IDs are available.
        root.setVisibility(View.GONE);
    }
    @Override protected void refresh() {
        setTitle(getString(id==0?R.string.admin_title_product_add:R.string.admin_title_product_edit));
        if(initialized) {
            this.<List<AdminCategory>>load(adminViewModel::categories,data -> { categories=data; dropdown(); });
            return;
        }
        this.<AdminViewModel.Catalog>load(cb -> adminViewModel.form(id,cb),data -> {
            source=data.product; categories=data.categories; categoryId=source.categoryId;
            edit(R.id.inputNameLayout).setText(source.name);
            edit(R.id.inputPriceLayout).setText(id==0?"":source.price.toPlainString());
            edit(R.id.inputStockLayout).setText(String.valueOf(source.stock));
            edit(R.id.inputDescriptionLayout).setText(source.description);
            edit(R.id.inputImageLayout).setText(source.image);
            edit(R.id.inputSizesLayout).setText(android.text.TextUtils.join(", ",source.sizes));
            edit(R.id.inputSoldCountLayout).setText(String.valueOf(source.soldCount));
            edit(R.id.inputForAiLayout).setText(source.forAI==null || source.forAI==org.json.JSONObject.NULL?"null":
                source.forAI instanceof String?org.json.JSONObject.quote((String)source.forAI):source.forAI.toString());
            if(draft!=null) {
                categoryId=draft.getLong("categoryId",categoryId);
                for(int inputId:INPUTS) edit(inputId).setText(draft.getString(String.valueOf(inputId),""));
            }
            dropdown(); initialized=true; preview();
            if(draft==null) original=snapshot();
        });
    }
    private void dropdown() {
        List<String> labels=new ArrayList<>(); labels.add(getString(R.string.admin_no_category));
        int selected=0;
        for(AdminCategory c:categories) { labels.add(c.name); if(c.id==categoryId) selected=labels.size()-1; }
        AutoCompleteTextView input=root.findViewById(R.id.inputCategory);
        AdminUi.dropdown(input,labels);
        input.setText(selected==0 && categoryId!=0?source.category:labels.get(selected),false);
    }
    private EditText edit(int id) { return ((TextInputLayout)root.findViewById(id)).getEditText(); }
    private String value(int id) { return edit(id).getText().toString().trim(); }
    private void error(int id,int message) {
        if(id==R.id.inputForAiLayout || id==R.id.inputSoldCountLayout)
            root.findViewById(R.id.productAdvancedFields).setVisibility(View.VISIBLE);
        TextInputLayout input=root.findViewById(id); input.setError(getString(message)); input.requestFocus();
    }
    private boolean imageValid(String text) {
        if(text.isEmpty()) return true;
        android.net.Uri uri=android.net.Uri.parse(text);
        return ("https".equalsIgnoreCase(uri.getScheme())||"http".equalsIgnoreCase(uri.getScheme()))
            && uri.getHost()!=null && !uri.getHost().isEmpty() && text.length()<=2048;
    }
    private void preview() {
        ImageView image=root.findViewById(R.id.productImagePreview);
        String url=value(R.id.inputImageLayout);
        if(!imageValid(url)) { error(R.id.inputImageLayout,R.string.admin_error_image_url); return; }
        AdminImageLoader.load(image,url,R.drawable.admin_image_placeholder);
    }
    private void save() {
        if(!initialized || adminViewModel.busy("product:"+id)) return;
        for(int box:INPUTS) ((TextInputLayout)root.findViewById(box)).setError(null);
        String name=value(R.id.inputNameLayout);
        if(name.isEmpty()||name.length()>255) { error(R.id.inputNameLayout,R.string.admin_error_required); return; }
        AdminProduct p=new AdminProduct(); p.id=id; p.name=name; p.categoryId=categoryId;
        p.description=value(R.id.inputDescriptionLayout); p.image=value(R.id.inputImageLayout);
        try {
            p.price=new BigDecimal(value(R.id.inputPriceLayout));
            if(p.price.signum()<0 || p.price.scale()>2 || p.price.compareTo(new BigDecimal("9999999999999.99"))>0)
                throw new NumberFormatException();
        } catch(NumberFormatException e) { error(R.id.inputPriceLayout,R.string.admin_error_price_decimal); return; }
        try { p.stock=Integer.parseInt(value(R.id.inputStockLayout)); if(p.stock<0) throw new NumberFormatException(); }
        catch(NumberFormatException e) { error(R.id.inputStockLayout,R.string.admin_error_stock); return; }
        try { p.soldCount=Integer.parseInt(value(R.id.inputSoldCountLayout)); if(p.soldCount<0) throw new NumberFormatException(); }
        catch(NumberFormatException e) { error(R.id.inputSoldCountLayout,R.string.admin_error_nonnegative); return; }
        if(!imageValid(p.image)) { error(R.id.inputImageLayout,R.string.admin_error_image_url); return; }
        String sizes=value(R.id.inputSizesLayout);
        if(sizes.equals(android.text.TextUtils.join(", ",source.sizes))) p.sizes.addAll(source.sizes);
        else if(!sizes.isEmpty()) for(String size:sizes.split(",",-1)) {
            String trimmed=size.trim();
            if(trimmed.isEmpty()||trimmed.length()>50) { error(R.id.inputSizesLayout,R.string.admin_error_size); return; }
            p.sizes.add(trimmed);
        }
        try {
            String json=value(R.id.inputForAiLayout);
            if(json.isEmpty()) json="null";
            // Validate standard JSON before converting to org.json's body representation.
            com.google.gson.stream.JsonReader reader=new com.google.gson.stream.JsonReader(new java.io.StringReader(json));
            reader.setLenient(false);
            JsonElement parsed=com.google.gson.internal.Streams.parse(reader);
            if(reader.peek()!=com.google.gson.stream.JsonToken.END_DOCUMENT) throw new IllegalArgumentException();
            p.forAI=new org.json.JSONTokener(parsed.toString()).nextValue();
        } catch(Exception e) { error(R.id.inputForAiLayout,R.string.admin_error_json); return; }
        this.<AdminProduct>action("product:"+id,cb -> adminViewModel.saveProduct(p,cb),saved -> {
            leaving=true; toast(getString(R.string.admin_saved)); AdminRouter.back(this);
        },R.id.btnSaveProduct,R.id.btnCancelProduct);
    }
    private String snapshot() {
        StringBuilder result=new StringBuilder().append(categoryId);
        for(int input:INPUTS) result.append('|').append(value(input));
        return result.toString();
    }
    private Bundle draft() {
        Bundle state=new Bundle(); state.putLong("categoryId",categoryId);
        for(int input:INPUTS) state.putString(String.valueOf(input),value(input));
        return state;
    }
    private void requestLeave() {
        if(adminViewModel.busy("product:"+id)) { toast(getString(R.string.admin_busy)); return; }
        if(!initialized||leaving||snapshot().equals(original)) { AdminRouter.back(this); return; }
        new MaterialAlertDialogBuilder(themedContext()).setTitle(R.string.admin_unsaved_title)
            .setMessage(R.string.admin_unsaved_message).setNegativeButton(R.string.admin_close,null)
            .setPositiveButton(R.string.admin_discard,(d,which) -> { leaving=true; AdminRouter.back(this); }).show();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state); state.putString("original",original);
        state.putBundle("draft",initialized && root!=null?draft():draft);
    }
    @Override public void onDestroyView() {
        if(initialized && root!=null) draft=draft();
        initialized=false; super.onDestroyView();
    }
}
