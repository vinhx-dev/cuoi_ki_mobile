package com.example.ecom.feature.admin.fragments;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import com.google.android.material.chip.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.data.AdminProduct;
import com.example.ecom.feature.admin.navigation.AdminRouter;
import java.util.*;
public class AdminProductFormFragment extends AdminFragment {
    private final ArrayList<String> photos=new ArrayList<>();
    private int id;
    private String imageKey="tee",original="",savedSizes,savedColors;
    private boolean restored,leaving;
    private final ActivityResultLauncher<String[]> picker=registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),uris-> {
        for(Uri uri:uris) {
            if(photos.contains(uri.toString()))continue;
            if(photos.size()>=6) {
                toast(getString(R.string.admin_max_images));
                break;
            }
            try {
                requireContext().getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            catch(SecurityException ignored) {
            }
            photos.add(uri.toString());
        }
        if(root!=null)renderPhotos();
    }
    );
    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        if(state!=null) {
            restored=true;
            ArrayList<String> saved=state.getStringArrayList("photos");
            if(saved!=null)photos.addAll(saved);
            savedSizes=state.getString("sizes");
            savedColors=state.getString("colors");
            original=state.getString("original","");
        }
    }
    @Override protected int layout() {
        return R.layout.fragment_admin_product_form;
    }
    @Override protected void bind(View view) {
        id=requireArguments().getInt(AdminRouter.ARG_ADMIN_PRODUCT_ID,0);
        AdminProduct p=id==0?null:adminViewModel.product(id);
        if(id!=0&&p==null) {
            view.post(()-> {
                if(isUiActive()) {
                    toast(getString(R.string.admin_not_found));
                    AdminRouter.back(this);
                }
            }
            );
            return;
        }
        setTitle(getString(id==0?R.string.admin_title_product_add:R.string.admin_title_product_edit));
        AutoCompleteTextView category=view.findViewById(R.id.inputCategory);
        AdminUi.dropdown(category,adminViewModel.categories());
        if(p!=null) {
            edit(R.id.inputNameLayout).setText(p.name);
            category.setText(p.category,false);
            edit(R.id.inputPriceLayout).setText(String.valueOf(p.price));
            edit(R.id.inputStockLayout).setText(String.valueOf(p.stock));
            edit(R.id.inputDescriptionLayout).setText(p.description);
            imageKey=p.imageKey;
            if(!restored)photos.addAll(p.images);
        }
        addChips((ChipGroup)view.findViewById(R.id.sizeChips),R.array.admin_sizes,savedSizes!=null?savedSizes:p==null?"":p.sizes);
        addChips((ChipGroup)view.findViewById(R.id.colorChips),R.array.admin_colors,savedColors!=null?savedColors:p==null?"":p.colors);
        int[] inputs = {R.id.inputNameLayout, R.id.inputCategoryLayout, R.id.inputPriceLayout, R.id.inputStockLayout, R.id.inputDescriptionLayout};
        for (int inputId : inputs) {
            TextInputLayout input = view.findViewById(inputId);
            AdminUi.watch(input.getEditText(), () -> input.setError(null));
        }
        renderPhotos();
        if(!restored)original=snapshot();
        view.findViewById(R.id.btnAddImages).setOnClickListener(v->picker.launch(new String[] {
            "image/*"
        }
        ));
        view.findViewById(R.id.btnSaveProduct).setOnClickListener(v->save());
        view.findViewById(R.id.btnCancelProduct).setOnClickListener(v->requestLeave());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                requestLeave();
            }
        }
        );
    }
    @Override protected void refresh() {
        setTitle(getString(id == 0 ? R.string.admin_title_product_add : R.string.admin_title_product_edit));
    }
    private EditText edit(int id) {
        return ((TextInputLayout)root.findViewById(id)).getEditText();
    }
    private String value(int id) {
        return edit(id).getText().toString().trim();
    }
    private void addChips(ChipGroup group,int choices,String selected) {
        for(String choice:getResources().getStringArray(choices)) {
            Chip chip=new Chip(themedContext());
            chip.setId(View.generateViewId());
            chip.setText(choice);
            chip.setCheckable(true);
            chip.setChecked(Arrays.asList(selected.split(", ")).contains(choice));
            group.addView(chip);
        }
    }
    private String checked(int id) {
        ChipGroup group=root.findViewById(id);
        List<String> values=new ArrayList<>();
        for(int i=0;i<group.getChildCount();i++) {
            Chip chip=(Chip)group.getChildAt(i);
            if(chip.isChecked())values.add(chip.getText().toString());
        }
        return android.text.TextUtils.join(", ",values);
    }
    private void renderPhotos() {
        LinearLayout box=root.findViewById(R.id.productImages);
        box.removeAllViews();
        if(photos.isEmpty()) {
            ImageView image=thumbnail();
            image.setImageResource(AdminUi.image(imageKey));
            image.setContentDescription(getString(R.string.admin_images));
            box.addView(image);
        }
        else for(int i=0;i<photos.size();i++) {
            String uri=photos.get(i);
            ImageView image=thumbnail();
            AdminImageLoader.load(image,uri,AdminUi.image(imageKey));
            image.setContentDescription(getString(R.string.admin_remove_image,i+1));
            image.setOnClickListener(v-> {
                photos.remove(uri);
                renderPhotos();
            }
            );
            box.addView(image);
        }
    }
    private ImageView thumbnail() {
        ImageView image=new ImageView(themedContext());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(AdminUi.dp(requireContext(),100),AdminUi.dp(requireContext(),112));
        p.setMarginEnd(AdminUi.dp(requireContext(),10));
        image.setLayoutParams(p);
        image.setBackgroundResource(R.drawable.admin_image_background);
        image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return image;
    }
    private void error(int id,int message) {
        TextInputLayout input=root.findViewById(id);
        input.setError(getString(message));
        input.requestFocus();
    }
    private void save() {
        int[] boxes= {
            R.id.inputNameLayout,R.id.inputCategoryLayout,R.id.inputPriceLayout,R.id.inputStockLayout,R.id.inputDescriptionLayout
        }
        ;
        for(int box:boxes)((TextInputLayout)root.findViewById(box)).setError(null);
        String name=value(R.id.inputNameLayout),category=((AutoCompleteTextView)root.findViewById(R.id.inputCategory)).getText().toString().trim(),description=value(R.id.inputDescriptionLayout);
        long price;
        int stock;
        if(name.isEmpty()) {
            error(R.id.inputNameLayout,R.string.admin_error_required);
            return;
        }
        if(name.length()>120) {
            error(R.id.inputNameLayout,R.string.admin_error_name);
            return;
        }
        if(!adminViewModel.categories().contains(category)) {
            error(R.id.inputCategoryLayout,R.string.admin_error_category);
            return;
        }
        try {
            price=Long.parseLong(value(R.id.inputPriceLayout));
            if(price<1||price>1000000000L)throw new NumberFormatException();
        }
        catch(NumberFormatException e) {
            error(R.id.inputPriceLayout,R.string.admin_error_price);
            return;
        }
        try {
            stock=Integer.parseInt(value(R.id.inputStockLayout));
            if(stock<0||stock>999999)throw new NumberFormatException();
        }
        catch(NumberFormatException e) {
            error(R.id.inputStockLayout,R.string.admin_error_stock);
            return;
        }
        if(description.length()>2000) {
            error(R.id.inputDescriptionLayout,R.string.admin_error_description);
            return;
        }
        AdminProduct product=new AdminProduct(id,name,category,price,stock,description,checked(R.id.sizeChips),checked(R.id.colorChips),imageKey);
        product.images.addAll(photos);
        adminViewModel.saveProduct(product);
        leaving=true;
        toast(getString(R.string.admin_saved));
        AdminRouter.back(this);
    }
    private String snapshot() {
        return value(R.id.inputNameLayout)+"|"+((AutoCompleteTextView)root.findViewById(R.id.inputCategory)).getText()+"|"+value(R.id.inputPriceLayout)+"|"+value(R.id.inputStockLayout)+"|"+value(R.id.inputDescriptionLayout)+"|"+checked(R.id.sizeChips)+"|"+checked(R.id.colorChips)+"|"+photos;
    }
    private void requestLeave() {
        if(root==null)return;
        if(leaving||snapshot().equals(original)) {
            AdminRouter.back(this);
            return;
        }
        new MaterialAlertDialogBuilder(themedContext()).setTitle(R.string.admin_unsaved_title).setMessage(R.string.admin_unsaved_message).setNegativeButton(R.string.admin_close,null).setPositiveButton(R.string.admin_discard,(d,which)-> {
            leaving=true;
            AdminRouter.back(this);
        }
        ).show();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putStringArrayList("photos",photos);
        state.putString("original",original);
        if(root!=null) {
            state.putString("sizes",checked(R.id.sizeChips));
            state.putString("colors",checked(R.id.colorChips));
        }
    }
}
