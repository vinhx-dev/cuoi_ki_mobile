package com.example.ecom.feature.product;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecom.core.data.*;
import com.example.ecom.core.ui.shop.ShopViews;
import com.example.ecom.core.ui.format.Formatter;
import com.google.android.material.bottomsheet.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.*;
/** A lifecycle-managed sheet, restored with quantity, mode and selected size. */
public class ProductOptionBottomSheet extends BottomSheetDialogFragment {
    private int quantity=1;
    private String size="";
    public static ProductOptionBottomSheet create(int id,boolean buy) {
        ProductOptionBottomSheet s=new ProductOptionBottomSheet();
        Bundle b=new Bundle();
        b.putInt("productId",id);
        b.putBoolean("buy",buy);
        s.setArguments(b);
        return s;
    }
    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        if(state!=null) {
            quantity=state.getInt("quantity",1);
            size=state.getString("size","");
        }
    }
    @Override public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("quantity",quantity);
        out.putString("size",size);
    }
    @NonNull @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup parent,@Nullable Bundle state) {
        android.content.Context c=requireContext();
        ShopProduct p=ShopCatalog.get(requireArguments().getInt("productId"));
        if(p==null&&getParentFragment()!=null)p=new ViewModelProvider(requireParentFragment()).get(ProductDetailViewModel.class).product.getValue();
        final ShopProduct product=p;
        boolean buy=requireArguments().getBoolean("buy");
        ScrollView scroll=new ScrollView(c);
        LinearLayout content=ShopViews.column(c);
        int pad=ShopViews.dp(c,20);
        content.setPadding(pad,ShopViews.dp(c,10),pad,pad);
        android.graphics.drawable.GradientDrawable contentShape=ShopViews.background(0xFFFFFFFF,24,c);
        float corner=ShopViews.dp(c,24);
        contentShape.setCornerRadii(new float[]{corner,corner,corner,corner,0,0,0,0});
        content.setBackground(contentShape);
        scroll.addView(content);
        View handle=new View(c);
        handle.setBackground(ShopViews.background(0xFFBDBDBD,3,c));
        LinearLayout.LayoutParams handleLp=new LinearLayout.LayoutParams(ShopViews.dp(c,40),ShopViews.dp(c,4));
        handleLp.gravity=Gravity.CENTER;
        handleLp.bottomMargin=ShopViews.dp(c,12);
        content.addView(handle,handleLp);
        LinearLayout title=ShopViews.row(c);
        TextView heading=ShopViews.text(c,buy?"Mua ngay":"Thêm vào giỏ hàng",20,true);
        title.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        MaterialButton close=ShopViews.button(c,"Đóng");
        close.setOnClickListener(v->dismiss());
        title.addView(close);
        content.addView(title);
        if(product==null) {
            content.addView(ShopViews.text(c,"Không thể tải sản phẩm. Vui lòng đóng và mở lại.",14,false));
            return scroll;
        }
        int available=buy?product.stock:Math.max(0,product.stock-LocalCart.quantity(c,product.id));
        quantity=Math.max(1,Math.min(quantity,Math.max(1,available)));
        LinearLayout summary=ShopViews.row(c);
        ImageView image=ShopViews.image(c,product,100);
        summary.addView(image,new LinearLayout.LayoutParams(ShopViews.dp(c,100),ShopViews.dp(c,100)));
        LinearLayout info=ShopViews.column(c);
        info.setPadding(ShopViews.dp(c,16),0,0,0);
        info.addView(ShopViews.text(c,product.name,15,true));
        TextView total=ShopViews.text(c,"",24,true);
        total.setTextColor(ShopViews.ORANGE);
        info.addView(total);
        summary.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        content.addView(summary);
        LinearLayout stepper=ShopViews.row(c);
        MaterialButton minus=ShopViews.button(c,"−"),plus=ShopViews.button(c,"+");
        TextView count=ShopViews.text(c,"",18,true);
        count.setGravity(Gravity.CENTER);
        stepper.addView(minus,new LinearLayout.LayoutParams(ShopViews.dp(c,64),-2));
        stepper.addView(count,new LinearLayout.LayoutParams(ShopViews.dp(c,56),-2));
        stepper.addView(plus,new LinearLayout.LayoutParams(ShopViews.dp(c,64),-2));
        content.addView(stepper);
        Runnable render=()-> {
            count.setText(String.valueOf(quantity));
            total.setText(Formatter.money(product.price*quantity));
            minus.setEnabled(quantity>1);
            plus.setEnabled(quantity<available);
        }
        ;
        minus.setOnClickListener(v-> {
            quantity=Math.max(1,quantity-1);
            render.run();
        }
        );
        plus.setOnClickListener(v-> {
            quantity=Math.min(available,quantity+1);
            render.run();
        }
        );
        render.run();
        content.addView(ShopViews.heading(c,"Chọn size"));
        ChipGroup sizes=new ChipGroup(c);
        sizes.setSingleSelection(true);
        sizes.setSelectionRequired(true);
        for(String label:new String[] {
            "S","M","L","XL"
        }
        ) {
            Chip chip=new Chip(c);
            chip.setId(View.generateViewId());
            chip.setText(label);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColor(new android.content.res.ColorStateList(new int[][] {
                new int[] {
                    android.R.attr.state_checked
                }
                ,new int[] {
                }
            }
            ,new int[] {
                0xFFFFE5D1,0xFFF8F9FA
            }
            ));
            chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(ShopViews.ORANGE));
            chip.setChipStrokeWidth(ShopViews.dp(c,1));
            chip.setChecked(label.equals(size));
            chip.setOnCheckedChangeListener((b,checked)-> {
                if(checked)size=label;
            }
            );
            sizes.addView(chip);
        }
        content.addView(sizes);
        TextView error=ShopViews.text(c,"",13,false);
        error.setTextColor(0xFFB3261E);
        content.addView(error);
        MaterialButton confirm=ShopViews.button(c,"Xác nhận");
        confirm.setEnabled(available>0);
        content.addView(confirm,new LinearLayout.LayoutParams(-1,-2));
        if(available==0)error.setText("Đã hết tồn kho khả dụng; kiểm tra số lượng trong giỏ hàng.");
        confirm.setOnClickListener(v-> {
            if(size.isEmpty()) {
                error.setText("Vui lòng chọn size trước khi xác nhận.");
                return;
            }
            if(quantity<1||quantity>available) {
                error.setText("Số lượng vượt tồn kho.");
                return;
            }
            if(!buy) {
                if(LocalCart.add(c,product,size,quantity)) {
                    Toast.makeText(c,"Đã thêm "+quantity+" sản phẩm, size "+size+" vào giỏ hàng",Toast.LENGTH_SHORT).show();
                    dismiss();
                }
                else error.setText("Số lượng trong giỏ đã đạt tồn kho. Vui lòng giảm số lượng.");
            }
            else if(product.isSample()) {
                error.setText("Đây là sản phẩm mẫu. Cần kết nối cửa hàng để đặt mua; chưa tạo đơn hàng.");
            }
            else {
                Bundle result=new Bundle();
                result.putInt("quantity",quantity);
                result.putString("size",size);
                getParentFragmentManager().setFragmentResult("purchase",result);
                dismiss();
            }
        }
        );
        return scroll;
    }
    @Override public void onStart() {
        super.onStart();
        if(getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog d=(BottomSheetDialog)getDialog();
            android.widget.FrameLayout sheet=d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if(sheet!=null) {
                android.graphics.drawable.GradientDrawable shape=ShopViews.background(0xFFFFFFFF,24,requireContext());
                    float radius=ShopViews.dp(requireContext(),24);
                    shape.setCornerRadii(new float[]{radius,radius,radius,radius,0,0,0,0});
                    sheet.setBackground(shape);
                BottomSheetBehavior.from(sheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(sheet,(v,insets)-> {
                    androidx.core.graphics.Insets bars=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                    v.setPadding(bars.left,0,bars.right,bars.bottom);
                    return insets;
                }
                );
            }
            d.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            d.getWindow().setDimAmount(0.35f);
        }
    }
}
