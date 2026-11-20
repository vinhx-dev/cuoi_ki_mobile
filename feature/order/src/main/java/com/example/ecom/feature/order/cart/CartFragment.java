package com.example.ecom.feature.order.cart;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecom.core.data.LocalCart;
import com.example.ecom.core.data.ShopProduct;
import com.example.ecom.core.api.ApiCallback;
import com.example.ecom.core.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.core.ui.format.Formatter;
import com.example.ecom.feature.order.R;

import java.util.ArrayList;
import java.util.List;

/** Trang Giỏ hàng (Cart). */
public class CartFragment extends BaseFragment implements CartAdapter.Listener {

    private RecyclerView recyclerCart;
    private TextView textEmpty;
    private TextView textTotalPrice;
    private View btnDeleteSelected;
    private View btnCheckout;
    private CartAdapter adapter;
    private ApiClient api;
    private boolean busy;

    public CartFragment() {
        super(R.layout.fragment_cart);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header
        View btnBack = view.findViewById(R.id.headerBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> AppRouter.back(this));
        }

        ImageView btnMore = view.findViewById(R.id.btnMore);
        if (btnMore != null) {
            btnMore.setOnClickListener(this::showDropdownMenu);
        }

        // Top action xóa
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected);
        if (btnDeleteSelected != null) {
            btnDeleteSelected.setOnClickListener(v -> confirmDeleteSelectedItems());
        }

        // RecyclerView & Empty View
        recyclerCart = view.findViewById(R.id.recyclerCart);
        textEmpty = view.findViewById(R.id.textEmpty);

        adapter = new CartAdapter(this);
        recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerCart.setAdapter(adapter);

        // Bottom Checkout Bar
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> handleCheckout());
        }

        api = ApiClient.getInstance(requireContext());
        updateCartState();
        loadCartItems();
    }

    private void loadCartItems() {
        requestCart("GET", "/cart", null);
    }

    private void requestCart(String method, String path, JSONObject body) {
        if (busy) return;
        busy = true;
        View view = requireView();
        api.requestStrict(method, path, null, body, data -> {
            List<LocalCart.CartItem> items = new ArrayList<>();
            if (data == null) return items;
            JSONArray rows = ((JSONObject) data).getJSONArray("items");
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                int productId = row.getInt("productId");
                ShopProduct product = new ShopProduct(productId, row.getString("name"),
                        row.getLong("unitPrice"), 0, "", "", row.optString("image", ""));
                items.add(new LocalCart.CartItem(productId, row.isNull("size") ? "" : row.getString("size"),
                        Long.toString(row.getLong("id")), product, row.getInt("quantity")));
            }
            return items;
        }, new ApiCallback<List<LocalCart.CartItem>>() {
            @Override public void onSuccess(List<LocalCart.CartItem> items) {
                busy = false;
                if (getView() == null) return;
                if (getView() != view) {
                    loadCartItems();
                    return;
                }
                for (LocalCart.CartItem item : items) {
                    for (LocalCart.CartItem previous : adapter.getItems()) {
                        if (item.key.equals(previous.key)) item.checked = previous.checked;
                    }
                }
                adapter.submit(items);
                updateCartState();
            }

            @Override public void onError(String message, int code) {
                busy = false;
                if (getView() == view) toast(message);
                else if (getView() != null) loadCartItems();
            }
        });
    }

    private void showDropdownMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_cart_header, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_select_all) {
                adapter.setAllChecked(true);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void updateCartState() {
        List<LocalCart.CartItem> items = adapter.getItems();
        boolean isEmpty = items.isEmpty();

        if (textEmpty != null) {
            textEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerCart != null) {
            recyclerCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        // Cập nhật tổng tiền
        long total = adapter.calculateCheckedTotal();
        if (textTotalPrice != null) {
            textTotalPrice.setText(Formatter.money(total));
        }

        // Cập nhật nút Xóa trên góc phải
        int checkedCount = adapter.getCheckedCount();
        int totalCount = items.size();

        btnCheckout.setEnabled(checkedCount > 0);
        if (btnDeleteSelected != null) {
            if (checkedCount > 0) {
                btnDeleteSelected.setVisibility(View.VISIBLE);
                TextView textDelete = btnDeleteSelected.findViewById(R.id.textDeleteAction);
                if (textDelete != null) {
                    if (checkedCount == totalCount && totalCount > 0) {
                        textDelete.setText("Xóa tất cả");
                    } else {
                        textDelete.setText("Xóa sản phẩm");
                    }
                }
            } else {
                btnDeleteSelected.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onItemCheckChanged() {
        updateCartState();
    }

    @Override
    public void onItemQuantityChanged(LocalCart.CartItem item, int delta) {
        if (busy) return;
        int newQty = item.quantity + delta;
        if (newQty <= 0) {
            onItemDeleted(item);
        } else {
            requestCart("PUT", "/cart/items/" + item.key, ApiClient.json("quantity", newQty));
        }
    }

    @Override
    public void onItemDeleted(LocalCart.CartItem item) {
        if (busy) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa " + (item.product != null ? item.product.name : "sản phẩm này") + " khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    requestCart("DELETE", "/cart/items/" + item.key, null);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteSelectedItems() {
        if (busy) return;
        int checkedCount = adapter.getCheckedCount();
        if (checkedCount <= 0) return;

        boolean allChecked = adapter.isAllChecked();
        String title = allChecked ? "Xóa tất cả sản phẩm" : "Xóa sản phẩm đã chọn";
        String message = allChecked ? "Bạn có chắc chắn muốn xóa toàn bộ sản phẩm khỏi giỏ hàng?" : "Bạn có chắc chắn muốn xóa " + checkedCount + " sản phẩm đã chọn khỏi giỏ hàng?";
        String btnText = allChecked ? "Xóa tất cả" : "Xóa";

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, (dialog, which) -> {
                    if (busy) return;
                    if (allChecked) {
                        requestCart("DELETE", "/cart", null);
                    } else {
                        List<String> selected = new ArrayList<>();
                        for (LocalCart.CartItem item : adapter.getItems()) {
                            if (item.checked) selected.add(item.key);
                        }
                        removeSelected(selected, 0, requireView());
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void removeSelected(List<String> ids, int index, View view) {
        if (index == ids.size()) {
            busy = false;
            if (getView() != null) loadCartItems();
            return;
        }
        busy = true;
        api.requestStrict("DELETE", "/cart/items/" + ids.get(index), null, null, null,
                new ApiCallback<Void>() {
                    @Override public void onSuccess(Void ignored) {
                        removeSelected(ids, index + 1, view);
                    }

                    @Override public void onError(String message, int code) {
                        busy = false;
                        if (getView() == view) toast(message);
                        if (getView() != null) loadCartItems();
                    }
                });
    }

    private void handleCheckout() {
        if (busy) return;
        int checkedCount = adapter.getCheckedCount();
        if (checkedCount <= 0) {
            toast("Vui lòng tick chọn ít nhất 1 sản phẩm trong giỏ hàng để thanh toán!");
            return;
        }

        long total = adapter.calculateCheckedTotal();
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận thanh toán")
                .setMessage("Thanh toán " + checkedCount + " sản phẩm với tổng tiền " + Formatter.money(total) + "?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    if (busy) return;
                    JSONArray ids = new JSONArray();
                    for (LocalCart.CartItem item : adapter.getItems()) {
                        if (item.checked) ids.put(Long.parseLong(item.key));
                    }
                    if (ids.length() == 0) return;
                    busy = true;
                    View view = requireView();
                    api.requestStrict("POST", "/orders/checkout", null,
                            ApiClient.json("cartItemIds", ids), data -> ((JSONArray) data).length(),
                            new ApiCallback<Integer>() {
                                @Override public void onSuccess(Integer count) {
                                    busy = false;
                                    if (getView() == null) return;
                                    if (getView() != view) {
                                        loadCartItems();
                                        return;
                                    }
                                    toast("Đặt hàng thành công (" + count + " đơn)");
                                    loadCartItems();
                                    AppRouter.toTab(CartFragment.this, "orders");
                                }

                                @Override public void onError(String message, int code) {
                                    busy = false;
                                    if (getView() == view) toast(message);
                                    if (getView() != null) loadCartItems();
                                }
                            });
                })
                .setNegativeButton("Bỏ qua", null)
                .show();
    }
}
