package com.example.ecom.core.navigation;

import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavType;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Bộ dựng danh sách trang cho NavController (≈ createBrowserRouter trong React Router).
 * Bạn KHÔNG cần sửa file này. Khai báo trang ở AppPages.java.
 *
 * Mỗi trang được gắn sẵn các tham số ẩn để layout dùng chung đọc lại:
 *   ARG_LAYOUT: layout của trang (NONE / MAIN / DETAIL / ADMIN)
 *   ARG_ROUTE : đường dẫn của trang (để bottom bar biết tab nào đang chọn)
 */
public final class RouteTable {

    public static final String ARG_LAYOUT = "_layout";
    public static final String ARG_ROUTE = "_route";

    private final List<Page> pages = new ArrayList<>();
    private String startRoute;
    private String loginRoute;

    RouteTable() {}

    /** Khai báo 1 trang: đường dẫn, tên class Fragment, layout bọc ngoài, ai được vào. */
    public Page page(String route, String fragmentClass, LayoutType layout, Access access) {
        Page page = new Page(route, fragmentClass, layout, access);
        pages.add(page);
        return page;
    }

    /** Trang mở đầu tiên khi vào app. */
    public void start(String route) {
        this.startRoute = route;
    }

    /** Trang đăng nhập: nơi chuyển tới khi vào trang LOGIN_REQUIRED mà chưa đăng nhập. */
    public void loginPage(String route) {
        this.loginRoute = route;
    }

    String loginRoute() {
        return loginRoute;
    }

    /** Tìm trang khớp với đường dẫn cụ thể, vd. "product/5" khớp "product/{productId}". */
    Page match(String path) {
        String pathOnly = path.split("\\?", 2)[0];
        for (Page page : pages) {
            if (page.pattern.matcher(pathOnly).matches()) return page;
        }
        return null;
    }

    /** Dựng NavGraph và gắn vào NavController. */
    void install(NavController nav) {
        NavigatorProvider provider = nav.getNavigatorProvider();
        NavGraph graph = new NavGraph(provider.getNavigator(NavGraphNavigator.class));
        graph.setRoute(AppRouter.ROOT);

        for (Page page : pages) {
            checkClassExists(page);
            FragmentNavigator.Destination dest =
                    new FragmentNavigator.Destination(provider.getNavigator(FragmentNavigator.class));
            dest.setClassName(page.fragmentClass);
            for (Map.Entry<String, NavArgument> arg : page.args.entrySet()) {
                dest.addArgument(arg.getKey(), arg.getValue());
            }
            dest.addArgument(ARG_LAYOUT, stringArg(page.layout.name()));
            dest.addArgument(ARG_ROUTE, stringArg(page.route));
            dest.setRoute(page.route); // phải gọi sau khi đã khai báo tham số
            graph.addDestination(dest);
        }

        graph.setStartDestination(startRoute);
        nav.setGraph(graph, null);
    }

    private static void checkClassExists(Page page) {
        try {
            Class.forName(page.fragmentClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("AppPages: trang '" + page.route
                    + "' khai báo class không tồn tại: " + page.fragmentClass, e);
        }
    }

    private static NavArgument stringArg(String defaultValue) {
        return new NavArgument.Builder().setType(NavType.StringType).setDefaultValue(defaultValue).build();
    }

    /** 1 trang trong bảng. Dùng intArg / stringArg để khai báo tham số {xxx} trong đường dẫn. */
    public static final class Page {
        final String route;
        final String fragmentClass;
        final LayoutType layout;
        final Access access;
        final Map<String, NavArgument> args = new LinkedHashMap<>();
        /** "product/{productId}" -> regex "product/[^/]+" (bỏ phần ?query). */
        final Pattern pattern;

        Page(String route, String fragmentClass, LayoutType layout, Access access) {
            this.route = route;
            this.fragmentClass = fragmentClass;
            this.layout = layout;
            this.access = access;
            String pathOnly = route.split("\\?", 2)[0];
            this.pattern = Pattern.compile(pathOnly.replaceAll("\\{[^}]+\\}", "[^/]+"));
        }

        /** Tham số số nguyên bắt buộc, vd. {productId}. */
        public Page intArg(String name) {
            args.put(name, new NavArgument.Builder().setType(NavType.IntType).build());
            return this;
        }

        /** Tham số chuỗi tuỳ chọn, vd. ?query={query}. */
        public Page stringArg(String name, String defaultValue) {
            args.put(name, RouteTable.stringArg(defaultValue));
            return this;
        }
    }
}
