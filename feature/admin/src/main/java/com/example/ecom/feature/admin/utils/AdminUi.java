package com.example.ecom.feature.admin.utils;
import android.content.Context;
import android.view.View;
import android.widget.*;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.data.*;
import java.text.*;
import java.util.*;
public final class AdminUi {
    private AdminUi() {
    }
    public static final Locale VI=Locale.forLanguageTag("vi-VN");
    public static String money(long n) {
        return NumberFormat.getIntegerInstance(VI).format(n)+" đ";
    }
    public static String number(int n) {
        return NumberFormat.getIntegerInstance(VI).format(n);
    }
    public static String date(long n) {
        return new SimpleDateFormat("dd/MM/yyyy",VI).format(new Date(n));
    }
    public static String dateTime(long n) {
        return new SimpleDateFormat("dd/MM/yyyy · HH:mm",VI).format(new Date(n));
    }
    public static String normalize(String s) {
        return Normalizer.normalize(s==null?"":s,Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('đ','d').replace('Đ','D').toLowerCase(VI).trim();
    }
    public static void text(View root,int id,Object value) {
        ((TextView)root.findViewById(id)).setText(String.valueOf(value));
    }
    public static int dp(Context c,float n) {
        return (int)(n*c.getResources().getDisplayMetrics().density+.5f);
    }
    public static String status(Context c,String s) {
        int id;
        switch(s) {
            case "PENDING":id=R.string.admin_status_pending;
            break;
            case "CONFIRMED":id=R.string.admin_status_confirmed;
            break;
            case "PROCESSING":id=R.string.admin_status_processing;
            break;
            case "SHIPPING":id=R.string.admin_status_shipping;
            break;
            case "COMPLETED":id=R.string.admin_status_completed;
            break;
            default:id=R.string.admin_status_cancelled;
        }
        return c.getString(id);
    }
    public static int image(String key) {
        switch(key) {
            case "polo":return R.drawable.admin_product_polo;
            case "jeans":return R.drawable.admin_product_jeans;
            case "dress":return R.drawable.admin_product_dress;
            case "hoodie":return R.drawable.admin_product_hoodie;
            case "jacket":return R.drawable.admin_product_jacket;
            case "shirt":return R.drawable.admin_product_shirt;
            case "pants":return R.drawable.admin_product_pants;
            default:return R.drawable.admin_product_tee;
        }
    }
    public static void productImage(ImageView image,AdminProduct p) {
        image.setTag(null);
        image.setImageResource(image(p.imageKey));
        if(!p.images.isEmpty())AdminImageLoader.load(image,p.images.get(0),image(p.imageKey));
    }
    public static String customerName(AdminCustomer c) {
        return c==null?"":c.name;
    }
    public static void dropdown(AutoCompleteTextView input,List<String> options) {
        input.setAdapter(new ArrayAdapter<>(input.getContext(),android.R.layout.simple_dropdown_item_1line,options));
        input.setDropDownBackgroundResource(R.drawable.admin_popup_background);
        input.setOnClickListener(v -> input.showDropDown());
    }
    public static String initials(String name) {
        String[] parts=name.trim().split("\\s+");
        return parts.length==1?parts[0].substring(0,1):parts[0].substring(0,1)+parts[parts.length-1].substring(0,1);
    }
    public static void watch(android.widget.EditText input,Runnable action) {
        input.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {
            }
            public void onTextChanged(CharSequence s,int start,int before,int count) {
                action.run();
            }
            public void afterTextChanged(android.text.Editable e) {
            }
        }
        );
    }
}
