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

        // Cập nhật nút Xóa trên góc phải
        int checkedCount = adapter.getCheckedCount();
        int totalCount = items.size();

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

        boolean allChecked = adapter.isAllChecked();
        String title = allChecked ? "Xóa tất cả sản phẩm" : "Xóa sản phẩm đã chọn";
        String message = allChecked ? "Bạn có chắc chắn muốn xóa toàn bộ sản phẩm khỏi giỏ hàng?" : "Bạn có chắc chắn muốn xóa " + checkedCount + " sản phẩm đã chọn khỏi giỏ hàng?";
        String btnText = allChecked ? "Xóa tất cả" : "Xóa";

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, (dialog, which) -> {
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
            toast("Vui lòng tick chọn ít nhất 1 sản phẩm trong giỏ hàng để thanh toán!");
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
