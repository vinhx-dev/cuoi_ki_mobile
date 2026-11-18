package com.example.ecom.feature.admin.data;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.ecom.feature.admin.data.*;
import org.json.*;
import java.util.*;
/** Local demo repository. Replace with admin API when connecting the backend. */
public final class AdminLocalStore {
    private static AdminLocalStore instance;
    private final SharedPreferences prefs;
    private final List<AdminProduct> products=new ArrayList<>();
    private final List<AdminCustomer> customers=new ArrayList<>();
    private final List<AdminOrder> orders=new ArrayList<>();
    public static synchronized AdminLocalStore getInstance(Context context) {
        if(instance==null)instance=new AdminLocalStore(context.getApplicationContext());
        return instance;
    }
    private AdminLocalStore(Context context) {
        prefs=context.getSharedPreferences("shop_admin_demo_v1",Context.MODE_PRIVATE);
        if(!load()) {
            seed();
            persist();
        }
    }
    public List<String> categories() {
        return Arrays.asList("Áo thun","Áo polo","Áo sơ mi","Áo hoodie","Áo khoác","Quần","Váy");
    }
    public List<AdminProduct> getProducts() {
        return new ArrayList<>(products);
    }
    public List<AdminCustomer> getCustomers() {
        return new ArrayList<>(customers);
    }
    public List<AdminOrder> getOrders() {
        List<AdminOrder> result=new ArrayList<>(orders);
        result.sort((a,b)->Long.compare(b.createdAt,a.createdAt));
        return result;
    }
    public AdminProduct product(int id) {
        for(AdminProduct p:products)if(p.id==id)return p;
        return null;
    }
    public AdminCustomer customer(int id) {
        for(AdminCustomer c:customers)if(c.id==id)return c;
        return null;
    }
    public AdminOrder order(int id) {
        for(AdminOrder o:orders)if(o.id==id)return o;
        return null;
    }
    public int customerOrderCount(int id) {
        int n=0;
        for(AdminOrder o:orders)if(o.customerId==id)n++;
        return n;
    }
    public int pendingCount() {
        int n=0;
        for(AdminOrder o:orders)if("PENDING".equals(o.status))n++;
        return n;
    }
    public int productCount() {
        return 248+products.size();
    }
    public int customerCount() {
        return 1320+customers.size();
    }
    public long dashboardRevenue() {
        return 12450000L;
    }
    public void saveProduct(AdminProduct p) {
        if(p.name==null||p.name.trim().isEmpty()||p.category==null||p.category.isEmpty()||p.price<=0||p.stock<0)throw new IllegalArgumentException("Invalid product");
        if(p.id==0) {
            int next=1;
            for(AdminProduct item:products)next=Math.max(next,item.id+1);
            p.id=next;
            products.add(0,p);
        }
        else {
            AdminProduct old=product(p.id);
            if(old==null)throw new IllegalArgumentException("Unknown product");
            products.set(products.indexOf(old),p);
        }
        persist();
    }
    public void deleteProduct(int id) {
        AdminProduct p=product(id);
        if(p!=null) {
            products.remove(p);
            persist();
        }
    }
    public boolean updateOrderStatus(int id,String next) {
        AdminOrder o=order(id);
        if(o==null)return false;
        if(!next.equals(o.nextStatus())&&!("CANCELLED".equals(next)&&o.canCancel()))return false;
        o.status=next;
        persist();
        return true;
    }
    public String setting(String key,String fallback) {
        return prefs.getString("setting_"+key,fallback);
    }
    public boolean notifications() {
        return prefs.getBoolean("notifications",true);
    }
    public void saveSettings(String name,String email,boolean notifications) {
        prefs.edit().putString("setting_name",name).putString("setting_email",email).putBoolean("notifications",notifications).apply();
    }
    private boolean load() {
        String raw=prefs.getString("data",null);
        if(raw==null)return false;
        try {
            JSONObject j=new JSONObject(raw);
            JSONArray a=j.getJSONArray("products");
            for(int i=0;i<a.length();i++)products.add(AdminProduct.fromJson(a.getJSONObject(i)));
            a=j.getJSONArray("customers");
            for(int i=0;i<a.length();i++)customers.add(AdminCustomer.fromJson(a.getJSONObject(i)));
            a=j.getJSONArray("orders");
            for(int i=0;i<a.length();i++)orders.add(AdminOrder.fromJson(a.getJSONObject(i)));
            return true;
        }
        catch(JSONException e) {
            Log.w("AdminLocalStore","Reset unreadable demo data",e);
            products.clear();
            customers.clear();
            orders.clear();
            return false;
        }
    }
    private void persist() {
        try {
            JSONArray p=new JSONArray(),c=new JSONArray(),o=new JSONArray();
            for(AdminProduct item:products)p.put(item.toJson());
            for(AdminCustomer item:customers)c.put(item.toJson());
            for(AdminOrder item:orders)o.put(item.toJson());
            prefs.edit().putString("data",new JSONObject().put("products",p).put("customers",c).put("orders",o).toString()).apply();
        }
        catch(JSONException e) {
            throw new IllegalStateException(e);
        }
    }
    private void seed() {
        String[] names= {
            "Áo thun cotton Essential","Áo polo Classic","Quần jeans Straight Fit","Váy midi thanh lịch","Áo hoodie Everyday","Áo khoác Linen","Áo sơ mi Oxford","Quần tây Relaxed"
        }
        ;
        String[] categories= {
            "Áo thun","Áo polo","Quần","Váy","Áo hoodie","Áo khoác","Áo sơ mi","Quần"
        }
        ;
        String[] images= {
            "tee","polo","jeans","dress","hoodie","jacket","shirt","pants"
        }
        ;
        long[] prices= {
            249000,329000,459000,599000,349000,699000,299000,449000
        }
        ;
        for(int i=0;i<names.length;i++)products.add(new AdminProduct(i+1,names[i],categories[i],prices[i],new int[] {
            48,32,25,16,38,12,42,20
        }
        [i],"Thiết kế dễ phối, chất liệu mềm mại và thoáng mát. Phù hợp mặc hằng ngày. Giặt nhẹ và phơi nơi thoáng mát.","S, M, L, XL","Đen, Trắng, Be",images[i]));
        String[] people= {
            "Nguyễn Minh Anh","Trần Hoàng Nam","Lê Ngọc Linh","Phạm Gia Hân","Võ Đức Huy","Đặng Thu Hà","Bùi Quốc Bảo","Đỗ Mai Phương"
        }
        ;
        for(int i=0;i<people.length;i++)customers.add(new AdminCustomer(i+1,people[i],new String[] {
            "minhanh","hoangnam","ngoclinh","giahan","duchuy","thuha","quocbao","maiphuong"
        }
        [i]+"@gmail.com","09012345"+String.format(Locale.US,"%02d",i+10),(25+i*7)+" Nguyễn Trãi, Phường Bến Thành, TP. Hồ Chí Minh"));
        String[] statuses= {
            "PENDING","CONFIRMED","PROCESSING","SHIPPING","COMPLETED","COMPLETED","CANCELLED"
        }
        ;
        for(int i=0;i<24;i++) {
            Calendar date=Calendar.getInstance();
            date.set(2026,Calendar.NOVEMBER,15-i/4,10+i%8,15,0);
            date.set(Calendar.MILLISECOND,0);
            AdminOrder o=new AdminOrder(1024-i,1+i%8,date.getTimeInMillis(),statuses[i%statuses.length]);
            AdminProduct item=products.get(i%8);
            o.items.add(new AdminOrderLine(item.name,item.category,item.price,1+i%2,"M",i%2==0?"Be":"Đen",item.imageKey));
            if(i%3==0) {
                item=products.get((i+2)%8);
                o.items.add(new AdminOrderLine(item.name,item.category,item.price,1,"L","Trắng",item.imageKey));
            }
            orders.add(o);
        }
    }
}
