package com.example.ecom.core.ui.shop;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import com.example.ecom.core.data.*;
import com.example.ecom.core.ui.R;
import com.example.ecom.core.ui.format.Formatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.function.Consumer;
public final class ShopViews {
    private ShopViews() {
    }
    public static final int ORANGE=Color.rgb(255,101,0), TEXT=Color.rgb(32,33,36), MUTED=Color.rgb(117,117,117);
    public static int dp(Context c,int n) {
        return Math.round(n*c.getResources().getDisplayMetrics().density);
    }
    public static LinearLayout column(Context c) {
        LinearLayout l=new LinearLayout(c);
        l.setOrientation(1);
        return l;
    }
    public static LinearLayout row(Context c) {
        LinearLayout l=new LinearLayout(c);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }
    public static TextView text(Context c,String s,int sp,boolean bold) {
        TextView t=new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(TEXT);
        if(bold)t.setTypeface(null,Typeface.BOLD);
        return t;
    }
    public static TextView heading(Context c,String s) {
        TextView t=text(c,s,21,true);
        t.setPadding(0,dp(c,20),0,dp(c,14));
        return t;
    }
    public static GradientDrawable background(int color,int radius,Context c) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c,radius));
        d.setStroke(dp(c,1),Color.rgb(229,231,235));
        return d;
    }
    public static MaterialButton button(Context c,String title) {
        MaterialButton b=new MaterialButton(c);
        b.setText(title);
        b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ORANGE));
        b.setCornerRadius(dp(c,12));
        b.setAllCaps(false);
        b.setMinHeight(dp(c,48));
        return b;
    }
    public static int icon(String category) {
        switch(category) {
            case "Quần":return R.drawable.shop_pants;
            case "Váy":return R.drawable.shop_dress;
            case "Giày":return R.drawable.shop_shoe;
            case "Túi xách":return R.drawable.shop_bag;
            case "Phụ kiện":return R.drawable.shop_glasses;
            case "Đồng hồ":return R.drawable.shop_watch;
            case "Mũ":return R.drawable.shop_cap;
            default:return R.drawable.shop_shirt;
        }
    }
    public static ImageView image(Context c,ShopProduct p,int height) {
        ImageView i=new ImageView(c);
        i.setImageResource(icon(p.category));
        i.setBackground(background(0xFFFFF4EA,12,c));
        i.setPadding(dp(c,30),dp(c,24),dp(c,30),dp(c,24));
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setContentDescription("Ảnh minh họa "+p.name+"; chưa có ảnh thực tế");
        i.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(c,height)));
        return i;
    }
    public static View card(Context c,ShopProduct p,Consumer<ShopProduct> click) {
        MaterialCardView card=new MaterialCardView(c);
        card.setRadius(dp(c,14));
        card.setCardElevation(dp(c,1));
        card.setStrokeWidth(dp(c,1));
        card.setStrokeColor(0xFFE5E7EB);
        card.setCardBackgroundColor(Color.WHITE);
        LinearLayout l=column(c);
        l.setBackgroundColor(Color.WHITE);
        l.addView(image(c,p,152));
        TextView name=text(c,p.name,14,true);
        name.setMinLines(2);
        name.setMaxLines(2);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        name.setPadding(dp(c,10),dp(c,10),dp(c,10),0);
        l.addView(name);
        TextView price=text(c,Formatter.money(p.price),16,true);
        price.setTextColor(ORANGE);
        price.setPadding(dp(c,10),dp(c,6),dp(c,10),dp(c,12));
        l.addView(price);
        card.addView(l);
        card.setOnClickListener(v->click.accept(p));
        card.setFocusable(true);
        card.setContentDescription(p.name+", "+Formatter.money(p.price));
        return card;
    }
    public static void showCart(Context c) {
        java.util.List<String> lines=LocalCart.lines(c);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(c).setTitle("Giỏ hàng cục bộ").setMessage(lines.isEmpty()?"Giỏ hàng đang trống":android.text.TextUtils.join("\n\n",lines)).setPositiveButton("Đóng",null).show();
    }
}
