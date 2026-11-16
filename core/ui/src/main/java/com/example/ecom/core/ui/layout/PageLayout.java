package com.example.ecom.core.ui.layout;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.ecom.core.navigation.AppPages;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.navigation.RouteTable;
import com.example.ecom.core.ui.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Layout bọc quanh 1 trang. ≈ <MainLayout>{children}</MainLayout> trong React.
 *
 * BaseFragment tự tạo PageLayout cho mỗi trang: inflate file XML của layout,
 * đặt nội dung trang vào chỗ trống (layoutContent), rồi gắn sự kiện cho các phần có trong layout:
 *   - headerBack  : quay lại
 *   - editSearch  : tìm kiếm (chờ ngừng gõ rồi mới tìm)
 *   - bottomBar   : chuyển tab
 *   - layoutLoading: lớp loading phủ lên trang
 * Phần nào layout không có thì bỏ qua, nên mọi layout dùng chung 1 class này.
 */
public final class PageLayout {

    /** Lắng nghe thanh tìm kiếm trên header. */
    public interface SearchListener {
        void onSearchQuery(String query);
    }

    /** Chờ người dùng ngừng gõ bao lâu mới tìm kiếm (tránh gọi API mỗi phím). */
    private static final long SEARCH_DEBOUNCE_MS = 400;

    private final Fragment page;
    private final View root;
    private final ViewGroup content;
    private final View loading;
    private final EditText editSearch;

    private int loadingCount = 0;
    private SearchListener searchListener;
    private boolean ignoreSearchChange = false;
    private final Runnable debouncedSearch = () -> {
        if (searchListener != null) searchListener.onSearchQuery(currentQuery());
    };

    public PageLayout(Fragment page, LayoutInflater inflater, ViewGroup container) {
        this.page = page;
        this.root = inflater.inflate(AppLayout.xmlOf(AppLayout.of(page.getArguments())), container, false);
        this.content = root.findViewById(R.id.layoutContent);
        this.loading = root.findViewById(R.id.layoutLoading);
        this.editSearch = root.findViewById(R.id.editSearch);

        View cart = root.findViewById(R.id.headerCart);
        if (cart != null) cart.setOnClickListener(v -> com.example.ecom.core.ui.shop.ShopViews.showCart(page.requireContext()));
        View camera = root.findViewById(R.id.iconCamera);
        if (camera != null) camera.setOnClickListener(v -> android.widget.Toast.makeText(page.requireContext(), "Tìm kiếm bằng ảnh chưa được hỗ trợ", android.widget.Toast.LENGTH_SHORT).show());
        View search = root.findViewById(R.id.iconSearch);
        if (search != null) search.setOnClickListener(v -> {
            if (searchListener != null) searchListener.onSearchQuery(currentQuery());
            else if (!currentQuery().isEmpty()) AppRouter.toHomeSearch(page,currentQuery());
        });
        setupBack();
        setupSearch();
        setupBottomBar();
    }

    public void dispose() { if (editSearch != null) editSearch.removeCallbacks(debouncedSearch); searchListener = null; }

    public View root() {
        return root;
    }

    /** Chỗ đặt nội dung trang. */
    public ViewGroup content() {
        return content;
    }

    // ---------- Loading ----------

    /** Bật/tắt loading (hỗ trợ nhiều request song song). */
    public void setLoading(boolean show) {
        loadingCount = Math.max(0, loadingCount + (show ? 1 : -1));
        loading.setVisibility(loadingCount > 0 ? View.VISIBLE : View.GONE);
    }

    // ---------- Nút back ----------

    private void setupBack() {
        View back = root.findViewById(R.id.headerBack);
        if (back != null) back.setOnClickListener(v -> AppRouter.back(page));
    }

    // ---------- Thanh tìm kiếm ----------

    /**
     * Trang tự xử lý tìm kiếm: hiện sẵn từ khoá hiện tại và nhận từ khoá mới.
     * Trang không gọi hàm này thì bấm "Tìm" trên bàn phím sẽ mở trang chủ với từ khoá đó.
     */
    public void bindSearch(String currentQuery, SearchListener listener) {
        if (editSearch == null) return;
        ignoreSearchChange = true;
        editSearch.setText(currentQuery);
        editSearch.setSelection(editSearch.length());
        ignoreSearchChange = false;
        searchListener = listener;
    }

    public void setSearchHint(CharSequence hint) {
        if (editSearch != null) editSearch.setHint(hint);
    }

    private void setupSearch() {
        if (editSearch == null) return;
        View btnClear = root.findViewById(R.id.btnClearSearch);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (ignoreSearchChange || searchListener == null) return;
                editSearch.removeCallbacks(debouncedSearch);
                editSearch.postDelayed(debouncedSearch, SEARCH_DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bấm "Tìm" trên bàn phím: tìm ngay, không chờ
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            editSearch.removeCallbacks(debouncedSearch);
            String query = currentQuery();
            if (searchListener != null) {
                searchListener.onSearchQuery(query);
            } else if (!query.isEmpty()) {
                AppRouter.toHomeSearch(page, query);
            }
            return true;
        });

        btnClear.setOnClickListener(v -> editSearch.setText(""));
    }

    private String currentQuery() {
        return editSearch.getText().toString().trim();
    }

    // ---------- Bottom bar ----------

    /** Tab nào mở trang nào. */
    private static final Tab[] TABS = {
            //      id trong main_bottom_bar_menu.xml   trang (AppPages)   đường dẫn để mở
            new Tab(R.id.tabHome,                       AppPages.HOME,     "home"),
            new Tab(R.id.tabOrders,                     AppPages.ORDERS,   "orders"),
            new Tab(R.id.tabProfile,                    AppPages.PROFILE,  "profile"),
    };

    private static final class Tab {
        final int menuId;
        final String route;
        final String path;

        Tab(int menuId, String route, String path) {
            this.menuId = menuId;
            this.route = route;
            this.path = path;
        }
    }

    private void setupBottomBar() {
        BottomNavigationView bar = root.findViewById(R.id.bottomBar);
        if (bar == null) return;
        bar.setItemActiveIndicatorEnabled(false);

        // Đánh dấu tab của trang hiện tại
        Bundle args = page.getArguments();
        String route = args != null ? args.getString(RouteTable.ARG_ROUTE) : null;
        for (Tab tab : TABS) {
            if (tab.route.equals(route)) bar.setSelectedItemId(tab.menuId);
        }

        bar.setOnItemSelectedListener(item -> {
            for (Tab tab : TABS) {
                if (tab.menuId == item.getItemId()) AppRouter.toTab(page, tab.path);
            }
            // false: không đổi tab trên trang cũ; trang mới tự đánh dấu tab của nó
            return false;
        });
        bar.setOnItemReselectedListener(item -> {
            // Category results belong to Home, but pressing Home must still return to the catalogue.
            if (AppPages.PRODUCT_LIST.equals(route) && item.getItemId() == R.id.tabHome) {
                AppRouter.toTab(page, "home");
            }
        });
    }
}
