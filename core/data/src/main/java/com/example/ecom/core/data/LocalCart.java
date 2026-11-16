package com.example.ecom.core.data;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.*;
/** Persistent cart scoped to the signed-in user; quantities are checked across all sizes. */
public final class LocalCart {
    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("customer_cart_"+new SessionManager(c).getUserName(),Context.MODE_PRIVATE);
    }
    public static synchronized int quantity(Context c,int id) {
        int count=0;
        for(Map.Entry<String,?> e:prefs(c).getAll().entrySet())if(e.getKey().startsWith(id+":"))count+=(Integer)e.getValue();
        return count;
    }
    public static synchronized boolean add(Context c,ShopProduct p,String size,int qty) {
        if(qty<1||!Arrays.asList("S","M","L","XL").contains(size)||quantity(c,p.id)+qty>p.stock)return false;
        SharedPreferences sp=prefs(c);
        String key=p.id+":"+size;
        sp.edit().putInt(key,sp.getInt(key,0)+qty).apply();
        return true;
    }
    public static List<String> lines(Context c) {
        List<String> r=new ArrayList<>();
        for(Map.Entry<String,?> e:prefs(c).getAll().entrySet()) {
            String[] key=e.getKey().split(":");
            ShopProduct p=ShopCatalog.get(Integer.parseInt(key[0]));
            r.add((p==null?"Sản phẩm #"+key[0]:p.name)+" · Size "+key[1]+" · SL "+e.getValue());
        }
        return r;
    }
}
