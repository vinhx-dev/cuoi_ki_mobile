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
    public static String money(Number n) {
        NumberFormat format=NumberFormat.getNumberInstance(VI);
        format.setMaximumFractionDigits(2);
        return format.format(n)+" đ";
    }
    public static String number(long n) {
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
            case "SHIPPING":id=R.string.admin_status_shipping;
            break;
            case "DELIVERED":
            id=R.string.admin_status_completed;
            break;
            case "CANCELLED":id=R.string.admin_status_cancelled;
            break;
            default:return s;
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
        AdminImageLoader.load(image,p.image,R.drawable.admin_image_placeholder);
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
        if(name==null||name.trim().isEmpty())return "?";
        String[] parts=name.trim().split("\\s+");
        return parts.length==1?parts[0].substring(0,1):parts[0].substring(0,1)+parts[parts.length-1].substring(0,1);
    }
    public static String userStatus(Context c,String status) {
        if("ACTIVE".equals(status))return c.getString(R.string.admin_user_active);
        if("BLOCKED".equals(status))return c.getString(R.string.admin_user_blocked);
        if("PENDING".equals(status))return c.getString(R.string.admin_user_pending);
        return status;
    }
    public static void badge(TextView view,String value,int foreground,int background) {
        view.setText(value);
        view.setTextColor(androidx.core.content.ContextCompat.getColor(view.getContext(),foreground));
        android.graphics.drawable.GradientDrawable shape=new android.graphics.drawable.GradientDrawable();
        shape.setColor(androidx.core.content.ContextCompat.getColor(view.getContext(),background));
        shape.setCornerRadius(dp(view.getContext(),8)); view.setBackground(shape);
        view.setPadding(dp(view.getContext(),9),dp(view.getContext(),5),dp(view.getContext(),9),dp(view.getContext(),5));
    }
    public static void orderBadge(TextView view,String status) {
        int color=R.color.admin_text_secondary,background=R.color.admin_background_secondary;
        switch(status) {
            case "PENDING":color=R.color.admin_warning;background=R.color.admin_warning_light;break;
            case "SHIPPING":color=R.color.admin_blue;background=R.color.admin_blue_light;break;
            case "DELIVERED":color=R.color.admin_success;background=R.color.admin_success_light;break;
            case "CANCELLED":color=R.color.admin_danger;background=R.color.admin_danger_light;break;
        }
        badge(view,status(view.getContext(),status),color,background);
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
