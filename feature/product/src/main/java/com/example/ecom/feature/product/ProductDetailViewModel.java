package com.example.ecom.feature.product;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.*;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.data.ShopProduct;
import com.example.ecom.core.ui.shop.CatalogRepository;
import com.example.ecom.feature.product.data.ProductApi;
public class ProductDetailViewModel extends AndroidViewModel {
    public final MutableLiveData<ShopProduct> product=new MutableLiveData<>();
    public final MutableLiveData<String> error=new MutableLiveData<>();
    public final MutableLiveData<Boolean> ordering=new MutableLiveData<>(false);
    private int loadedId=Integer.MIN_VALUE;
    public ProductDetailViewModel(@NonNull Application app) {
        super(app);
    }
    public void load(int id) {
        if(loadedId==id&&product.getValue()!=null)return;
        loadedId=id;
        new CatalogRepository(getApplication()).detail(id,new ApiCallback<ShopProduct>() {
            public void onSuccess(ShopProduct p) {
                product.setValue(p);
                error.setValue("");
            }
            public void onError(String message,int code) {
                error.setValue(message);
            }
        }
        );
    }
    public void placeOrder(int qty,String size,ApiCallback<Integer> cb) {
        ShopProduct p=product.getValue();
        if(p==null||p.isSample()||Boolean.TRUE.equals(ordering.getValue()))return;
        ordering.setValue(true);
        new ProductApi(getApplication()).createOrder(p.id,qty,size,new ApiCallback<Integer>() {
            public void onSuccess(Integer id) {
                ordering.setValue(false);
                cb.onSuccess(id);
            }
            public void onError(String m,int code) {
                ordering.setValue(false);
                cb.onError(m,code);
            }
        }
        );
    }
}
