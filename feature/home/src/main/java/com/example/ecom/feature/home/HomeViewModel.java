package com.example.ecom.feature.home;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.data.*;
import com.example.ecom.core.ui.shop.CatalogRepository;
import java.util.*;
public class HomeViewModel extends AndroidViewModel {
    public final MutableLiveData<List<ShopProduct>> products=new MutableLiveData<>();
    public final MutableLiveData<String> message=new MutableLiveData<>("Đang tải sản phẩm…");
    private List<ShopProduct> source=new ArrayList<>();
    public String query="",category="";
    private boolean loaded=false;
    private int request=0;
    public HomeViewModel(@NonNull Application app) {
        super(app);
    }
    public void load() {
        if(loaded)return;
        loaded=true;
        refresh();
    }
    public void search(String q) {
        query=q;
        render();
    }
    private void render() {
        List<ShopProduct> visible=ShopCatalog.filter(source,category,query);
        if(category.isEmpty()&&query.isEmpty()&&!visible.isEmpty()&&visible.get(0).isSample())visible=new ArrayList<>(visible.subList(0,Math.min(6,visible.size())));
        if(category.equals("Áo"))visible.removeIf(p->p.id==-1||p.id==-2);
        products.setValue(visible);
    }
    public void refresh() {
        int token=++request;
        message.setValue("Đang tải sản phẩm…");
        new CatalogRepository(getApplication()).list("",new ApiCallback<List<ShopProduct>>() {
            public void onSuccess(List<ShopProduct> p) {
                if(token!=request)return;
                source=p;
                message.setValue("Bộ sưu tập từ cửa hàng · Ảnh đang dùng hình minh họa");
                render();
            }
            public void onError(String error,int code) {
                if(token!=request)return;
                source=ShopCatalog.samples();
                message.setValue("Chế độ xem mẫu · Chưa kết nối được cửa hàng. Chạm để thử lại.");
                render();
            }
        }
        );
    }
}
