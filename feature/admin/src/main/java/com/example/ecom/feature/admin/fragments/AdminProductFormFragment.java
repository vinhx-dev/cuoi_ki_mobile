package com.example.ecom.feature.admin.fragments;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private Uri selectedImageUri, pendingImageUri;
    private String imageData="", imageError;
    private boolean imageRemoved, encoding;
    private int imageRequest;
    private final ActivityResultLauncher<String[]> pickImage=registerForActivityResult(
        new ActivityResultContracts.OpenDocument(),uri -> {
            if(uri==null) return;
            try { requireContext().getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION); }
            catch(SecurityException ignored) { /* The current grant can still be used; restore reports any access failure. */ }
            if(root==null || !initialized) { pendingImageUri=uri; return; }
            selectedImageUri=uri; imageRemoved=false; encodeImage();
        });
    private List<AdminCategory> categories = new ArrayList<>();
    private static final int[] INPUTS = {R.id.inputNameLayout,R.id.inputPriceLayout,R.id.inputStockLayout,
        R.id.inputDescriptionLayout,R.id.inputSizesLayout,R.id.inputSoldCountLayout,R.id.inputForAiLayout};

    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        if(state!=null) {
            draft=state.getBundle("draft"); original=state.getString("original","");
            String pending=state.getString("pendingImageUri");
            pendingImageUri=pending==null?null:Uri.parse(pending);
        }
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
        view.findViewById(R.id.btnAddImages).setOnClickListener(v -> pickImage.launch(new String[]{"image/*"}));
        view.findViewById(R.id.btnRemoveImage).setOnClickListener(v -> {
            imageRequest++; selectedImageUri=null; imageRemoved=true; encoding=false; imageError=null; imageData="";
            preview(); imageControls();
        });
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
            imageData=source.image; selectedImageUri=null; imageRemoved=false; imageError=null;
            edit(R.id.inputSizesLayout).setText(android.text.TextUtils.join(", ",source.sizes));
            edit(R.id.inputSoldCountLayout).setText(String.valueOf(source.soldCount));
            edit(R.id.inputForAiLayout).setText(source.forAI==null || source.forAI==org.json.JSONObject.NULL?"null":
                source.forAI instanceof String?org.json.JSONObject.quote((String)source.forAI):source.forAI.toString());
            if(draft!=null) {
                categoryId=draft.getLong("categoryId",categoryId);
                for(int inputId:INPUTS) edit(inputId).setText(draft.getString(String.valueOf(inputId),""));
                String selected=draft.getString("imageUri");
                selectedImageUri=selected==null?null:Uri.parse(selected);
                imageRemoved=draft.getBoolean("imageRemoved");
                if(imageRemoved) imageData="";
            }
            dropdown(); initialized=true; preview();
            if(draft==null) original=snapshot();
            if(pendingImageUri!=null) {
                selectedImageUri=pendingImageUri; pendingImageUri=null; imageRemoved=false;
            }
            if(selectedImageUri!=null) encodeImage(); else imageControls();
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
    private void preview() {
        ImageView image=root.findViewById(R.id.productImagePreview);
        AdminImageLoader.load(image,imageData,R.drawable.admin_image_placeholder);
    }
    private void encodeImage() {
        int request=++imageRequest;
        encoding=true; imageError=null; imageControls();
        AdminImageEncoder.encode(requireContext(),selectedImageUri,(encoded,error) -> {
            if(root==null || request!=imageRequest) return;
            encoding=false; imageError=error;
            if(error==null) imageData=encoded;
            preview(); imageControls();
        });
    }
    private void imageControls() {
        if(root==null) return;
        boolean busy=adminViewModel.busy("product:"+id);
        root.findViewById(R.id.btnSaveProduct).setEnabled(!busy && !encoding && imageError==null);
        root.findViewById(R.id.btnAddImages).setEnabled(!busy && !encoding);
        root.findViewById(R.id.btnRemoveImage).setEnabled(!busy && !encoding
            && (selectedImageUri!=null || !imageData.isEmpty()));
        TextView info=root.findViewById(R.id.textImageInfo);
        info.setText(imageError!=null?imageError:getString(encoding?R.string.admin_image_encoding:
            imageData.isEmpty()?R.string.admin_image_empty:R.string.admin_image_ready));
    }
    private void save() {
        if(!initialized || encoding || imageError!=null || adminViewModel.busy("product:"+id)) return;
        for(int box:INPUTS) ((TextInputLayout)root.findViewById(box)).setError(null);
        String name=value(R.id.inputNameLayout);
        if(name.isEmpty()||name.length()>255) { error(R.id.inputNameLayout,R.string.admin_error_required); return; }
        AdminProduct p=new AdminProduct(); p.id=id; p.name=name; p.categoryId=categoryId;
        p.description=value(R.id.inputDescriptionLayout); p.image=imageData;
        p.includeImage=id==0 || !Objects.equals(imageData,source.image);
        try {
            p.price=new BigDecimal(value(R.id.inputPriceLayout));
            if(p.price.signum()<0 || p.price.scale()>2 || p.price.compareTo(new BigDecimal("9999999999999.99"))>0)
                throw new NumberFormatException();
        } catch(NumberFormatException e) { error(R.id.inputPriceLayout,R.string.admin_error_price_decimal); return; }
        try { p.stock=Integer.parseInt(value(R.id.inputStockLayout)); if(p.stock<0) throw new NumberFormatException(); }
        catch(NumberFormatException e) { error(R.id.inputStockLayout,R.string.admin_error_stock); return; }
        try { p.soldCount=Integer.parseInt(value(R.id.inputSoldCountLayout)); if(p.soldCount<0) throw new NumberFormatException(); }
        catch(NumberFormatException e) { error(R.id.inputSoldCountLayout,R.string.admin_error_nonnegative); return; }
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
        },R.id.btnSaveProduct,R.id.btnCancelProduct,R.id.btnAddImages,R.id.btnRemoveImage);
    }
    private String snapshot() {
        StringBuilder result=new StringBuilder().append(categoryId);
        for(int input:INPUTS) result.append('|').append(value(input));
        result.append('|').append(imageRemoved?"removed":selectedImageUri==null?"original":selectedImageUri.toString());
        return result.toString();
    }
    private Bundle draft() {
        Bundle state=new Bundle(); state.putLong("categoryId",categoryId);
        for(int input:INPUTS) state.putString(String.valueOf(input),value(input));
        if(selectedImageUri!=null) state.putString("imageUri",selectedImageUri.toString());
        state.putBoolean("imageRemoved",imageRemoved);
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
        if(pendingImageUri!=null) state.putString("pendingImageUri",pendingImageUri.toString());
        state.putBundle("draft",initialized && root!=null?draft():draft);
    }
    @Override public void onDestroyView() {
        if(initialized && root!=null) draft=draft();
        imageRequest++; encoding=false; imageData="";
        initialized=false; super.onDestroyView();
    }
}
