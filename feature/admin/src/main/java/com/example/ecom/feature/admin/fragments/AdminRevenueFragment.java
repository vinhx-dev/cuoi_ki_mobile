package com.example.ecom.feature.admin.fragments;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.*;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.ecom.feature.admin.R;
import com.example.ecom.feature.admin.utils.*;
import com.example.ecom.feature.admin.models.ChartPoint;
import com.example.ecom.core.data.model.admin.*;
import java.text.*;
import java.util.*;
public class AdminRevenueFragment extends AdminFragment {
    private int mode=1;
    private Calendar selected=Calendar.getInstance();
    @Override public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        selected.set(2026,Calendar.NOVEMBER,15,0,0,0);
        selected.set(Calendar.MILLISECOND,0);
        if(state!=null) {
            selected.setTimeInMillis(state.getLong("date",selected.getTimeInMillis()));
            mode=state.getInt("mode");
        }
    }
    @Override protected int layout() {
        return R.layout.fragment_admin_revenue;
    }
    @Override protected void bind(View view) {
        AutoCompleteTextView period=view.findViewById(R.id.inputPeriod);
        List<String> modes=Arrays.asList(getString(R.string.admin_period_day),getString(R.string.admin_period_month),getString(R.string.admin_period_year));
        AdminUi.dropdown(period,modes);
        period.setText(modes.get(mode),false);
        period.setOnItemClickListener((a,b,index,id)-> {
            mode=index;
            refresh();
        }
        );
        view.findViewById(R.id.btnPeriod).setOnClickListener(v->choosePeriod());
    }
    private void choosePeriod() {
        if(mode==0) {
            Calendar utc=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            utc.clear();
            utc.set(selected.get(Calendar.YEAR),selected.get(Calendar.MONTH),selected.get(Calendar.DAY_OF_MONTH));
            MaterialDatePicker<Long> picker=MaterialDatePicker.Builder.datePicker().setTheme(R.style.ThemeOverlay_ShopAdmin_Calendar).setTitleText(R.string.admin_select_date).setSelection(utc.getTimeInMillis()).build();
            picker.addOnPositiveButtonClickListener(time-> {
                Calendar value=Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                value.setTimeInMillis(time);
                selected.set(value.get(Calendar.YEAR),value.get(Calendar.MONTH),value.get(Calendar.DAY_OF_MONTH),0,0,0);
                if(root!=null)refresh();
            }
            );
            picker.show(getChildFragmentManager(),"admin_date");
        }
        else if(mode==1) {
            String[] months=new DateFormatSymbols(AdminUi.VI).getMonths();
            String[] labels=Arrays.copyOf(months,12);
            new MaterialAlertDialogBuilder(themedContext()).setTitle(R.string.admin_select_month).setSingleChoiceItems(labels,selected.get(Calendar.MONTH),(d,index)-> {
                selected.set(Calendar.DAY_OF_MONTH,1);
                selected.set(Calendar.MONTH,index);
                d.dismiss();
                refresh();
            }
            ).setNegativeButton(R.string.admin_close,null).show();
        }
        else {
            String[] years= {
                "2024","2025","2026","2027","2028"
            }
            ;
            new MaterialAlertDialogBuilder(themedContext()).setTitle(R.string.admin_select_year).setItems(years,(d,index)-> {
                selected.set(Calendar.DAY_OF_MONTH,1);
                selected.set(Calendar.YEAR,Integer.parseInt(years[index]));
                refresh();
            }
            ).show();
        }
    }
    @Override protected void refresh() {
        Calendar start=(Calendar)selected.clone();
        if(mode>=1)start.set(Calendar.DAY_OF_MONTH,1);
        if(mode==2)start.set(Calendar.MONTH,0);
        start.set(Calendar.HOUR_OF_DAY,0);
        start.set(Calendar.MINUTE,0);
        start.set(Calendar.SECOND,0);
        start.set(Calendar.MILLISECOND,0);
        Calendar end=(Calendar)start.clone();
        end.add(mode==0?Calendar.DAY_OF_MONTH:mode==1?Calendar.MONTH:Calendar.YEAR,1);
        String pattern=mode==0?"dd/MM/yyyy":mode==1?"MM/yyyy":"yyyy";
        AdminUi.text(root,R.id.btnPeriod,new SimpleDateFormat(pattern,AdminUi.VI).format(selected.getTime()));
        int buckets=mode==0?6:mode==1?start.getActualMaximum(Calendar.DAY_OF_MONTH):12;
        long[] sums=new long[buckets];
        Map<String,Long> categories=new LinkedHashMap<>();
        long total=0;
        int count=0,sold=0;
        for(AdminOrder order:repository.getOrders())if("COMPLETED".equals(order.status)&&order.createdAt>=start.getTimeInMillis()&&order.createdAt<end.getTimeInMillis()) {
            total+=order.total();
            count++;
            Calendar time=Calendar.getInstance();
            time.setTimeInMillis(order.createdAt);
            int index=mode==0?time.get(Calendar.HOUR_OF_DAY)/4:mode==1?time.get(Calendar.DAY_OF_MONTH)-1:time.get(Calendar.MONTH);
            sums[index]+=order.total();
            for(AdminOrderLine line:order.items) {
                sold+=line.quantity;
                Long value=categories.get(line.category);
                categories.put(line.category,(value==null?0:value)+line.price*line.quantity);
            }
        }
        AdminUi.text(root,R.id.revenueTotal,AdminUi.money(total));
        AdminUi.text(root,R.id.revenueOrders,AdminUi.number(count));
        AdminUi.text(root,R.id.revenueSold,AdminUi.number(sold));
        List<ChartPoint> timePoints=new ArrayList<>();
        for(int i=0;i<buckets;i++)timePoints.add(new ChartPoint(mode==0?String.format(Locale.US,"%02dh",i*4):String.valueOf(i+1),sums[i]));
        ((RevenueChartView)root.findViewById(R.id.revenueChart)).setPoints(timePoints,false);
        List<ChartPoint> categoryPoints=new ArrayList<>();
        for(Map.Entry<String,Long> entry:categories.entrySet())categoryPoints.add(new ChartPoint(entry.getKey(),entry.getValue()));
        ((RevenueChartView)root.findViewById(R.id.categoryChart)).setPoints(categoryPoints,true);
    }
    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putLong("date",selected.getTimeInMillis());
        state.putInt("mode",mode);
    }
}
