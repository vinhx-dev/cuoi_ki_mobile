package com.example.ecom.core.data;
import java.util.*;
public final class ShopCatalog {
    private ShopCatalog() {
    }
    public static final List<String> CATEGORIES=Collections.unmodifiableList(Arrays.asList("Áo","Quần","Váy","Giày","Túi xách","Phụ kiện","Đồng hồ","Mũ"));
    private static final Map<Integer,ShopProduct> PRODUCTS=new LinkedHashMap<>();
    static {
        sample(-1,"Áo Khoác Hoodie",250000,"Áo");
        sample(-2,"Áo Khoác Váy",250000,"Áo");
        sample(-3,"Áo Thun Cotton",150000,"Áo");
        sample(-4,"Váy Nữ",320000,"Váy");
        sample(-5,"Áo Sơ Mi Caro",280000,"Áo");
        sample(-6,"Giày Thể Thao",750000,"Giày");
        sample(-7,"Áo Khoác Nhẹ",350000,"Áo");
        sample(-8,"Áo Polo Nam",199000,"Áo");
        sample(-9,"Áo Sơ Mi Trắng",250000,"Áo");
        sample(-10,"Áo Hoodie",450000,"Áo");
    }
    private static void sample(int id,String n,long p,String c) {
        PRODUCTS.put(id,new ShopProduct(id,n,p,20,c,
        id==-3 ? "Áo thun cotton sọc ngang phong cách thoáng mát, dễ mặc hằng ngày, phù hợp cho nhiều hoạt động dã ngoại dạo phố." : n+" dễ phối đồ, phù hợp sử dụng hằng ngày. Thông tin minh họa, cần cập nhật từ cửa hàng.",""));
    }
    public static synchronized void cache(ShopProduct p) {
        PRODUCTS.put(p.id,p);
    }
    public static synchronized ShopProduct get(int id) {
        return PRODUCTS.get(id);
    }
    public static synchronized List<ShopProduct> real() {
        List<ShopProduct> r=new ArrayList<>();
        for(ShopProduct p:PRODUCTS.values())if(!p.isSample())r.add(p);
        return r;
    }
    public static List<ShopProduct> samples() {
        List<ShopProduct> r=new ArrayList<>();
        for(int id:new int[] {
            -1,-2,-3,-4,-5,-6,-7,-8,-9,-10
        }
        )r.add(get(id));
        return r;
    }
    public static List<ShopProduct> filter(List<ShopProduct> source,String category,String query) {
        List<ShopProduct> r=new ArrayList<>();
        String q=query.toLowerCase(Locale.forLanguageTag("vi-VN")).trim();
        for(ShopProduct p:source) if((category.isEmpty()||category.equals(p.category))&&p.name.toLowerCase(Locale.forLanguageTag("vi-VN")).contains(q))r.add(p);
        if(category.equals("Áo")) r.sort(Comparator.comparingInt(p -> {
            int[] ids= {
                -3,-5,-7,-8,-9,-10,-1,-2
            }
            ;
            for(int i=0;i<ids.length;i++)if(ids[i]==p.id)return i;
            return 99;
        }
        ));
        return r;
    }
}
