package com.example.ecom.feature.order.cart;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecom.core.data.LocalCart;
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
    private CheckBox cbSelectAllTop;
    private View btnDeleteSelected;
    private View btnCheckout;
    private CartAdapter adapter;
    private boolean isUpdatingCheckState = false;

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

        // Top actions
        cbSelectAllTop = view.findViewById(R.id.cbSelectAllTop);
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected);

        if (cbSelectAllTop != null) {
            cbSelectAllTop.setOnCheckedChangeListener((btn, isChecked) -> {
                if (!isUpdatingCheckState && adapter != null) {
                    adapter.setAllChecked(isChecked);
                }
            });
        }

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

        loadCartItems();
    }

    private void loadCartItems() {
        List<LocalCart.CartItem> items = LocalCart.getItems(requireContext());
        adapter.submit(items);
        updateCartState();
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

        // Cập nhật nút Xóa tất cả được chọn
        int checkedCount = adapter.getCheckedCount();
        if (btnDeleteSelected != null) {
            btnDeleteSelected.setVisibility(checkedCount > 0 ? View.VISIBLE : View.GONE);
        }

        // Cập nhật CheckBox Chọn tất cả top
        isUpdatingCheckState = true;
        if (cbSelectAllTop != null) {
            cbSelectAllTop.setChecked(adapter.isAllChecked());
        }
        isUpdatingCheckState = false;
    }

    @Override
    public void onItemCheckChanged() {
        updateCartState();
    }

    @Override
    public void onItemQuantityChanged(LocalCart.CartItem item, int delta) {
        int newQty = item.quantity + delta;
        if (newQty <= 0) {
            onItemDeleted(item);
        } else {
            item.quantity = newQty;
            LocalCart.updateQuantity(requireContext(), item.key, newQty);
            adapter.notifyDataSetChanged();
            updateCartState();
        }
    }

    @Override
    public void onItemDeleted(LocalCart.CartItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa " + (item.product != null ? item.product.name : "sản phẩm này") + " khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    LocalCart.remove(requireContext(), item.key);
                    loadCartItems();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteSelectedItems() {
        int checkedCount = adapter.getCheckedCount();
        if (checkedCount <= 0) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa các sản phẩm đã chọn")
                .setMessage("Bạn có chắc chắn muốn xóa " + checkedCount + " sản phẩm đã chọn khỏi giỏ hàng?")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    List<LocalCart.CartItem> current = new ArrayList<>(adapter.getItems());
                    for (LocalCart.CartItem item : current) {
                        if (item.checked) {
                            LocalCart.remove(requireContext(), item.key);
                        }
                    }
                    loadCartItems();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void handleCheckout() {
        int checkedCount = adapter.getCheckedCount();
        if (checkedCount <= 0) {
            toast("Vui lòng chọn ít nhất 1 sản phẩm để thanh toán");
            return;
        }

        long total = adapter.calculateCheckedTotal();
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận thanh toán")
                .setMessage("Thanh toán " + checkedCount + " sản phẩm với tổng tiền " + Formatter.money(total) + "?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    // Xóa các món đã thanh toán khỏi giỏ hàng
                    List<LocalCart.CartItem> current = new ArrayList<>(adapter.getItems());
                    for (LocalCart.CartItem item : current) {
                        if (item.checked) {
                            LocalCart.remove(requireContext(), item.key);
                        }
                    }
                    toast("Đặt hàng thành công!");
                    AppRouter.toTab(this, "orders");
                })
                .setNegativeButton("Bỏ qua", null)
                .show();
    }
}
