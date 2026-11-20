package com.example.ecom.core.data;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ShopCatalog {

    private ShopCatalog() {}

    public static final List<String> CATEGORIES = Collections.unmodifiableList(
            Arrays.asList("Áo", "Quần", "Váy", "Giày", "Túi xách", "Phụ kiện", "Đồng hồ", "Mũ"));

    private static final Map<Integer, ShopProduct> PRODUCTS = new LinkedHashMap<>();

    static {
        sample(-1, "Áo Khoác Hoodie", 250000, "Áo");
        sample(-2, "Áo Khoác Váy", 250000, "Áo");
        sample(-3, "Áo Thun Cotton", 150000, "Áo");
        sample(-4, "Váy Nữ", 320000, "Váy");
        sample(-5, "Áo Sơ Mi Caro", 280000, "Áo");
        sample(-6, "Giày Thể Thao", 750000, "Giày");
        sample(-7, "Áo Khoác Nhẹ", 350000, "Áo");
        sample(-8, "Áo Polo Nam", 199000, "Áo");
        sample(-9, "Áo Sơ Mi Trắng", 250000, "Áo");
        sample(-10, "Áo Hoodie", 450000, "Áo");
    }

    private static void sample(int id, String n, long p, String c) {
        PRODUCTS.put(id, new ShopProduct(id, n, p, 20, c,
                id == -3 ? "Áo thun cotton sọc ngang phong cách thoáng mát, dễ mặc hằng ngày, phù hợp cho nhiều hoạt động dã ngoại dạo phố."
                         : n + " dễ phối đồ, phù hợp sử dụng hằng ngày. Thông tin minh họa, cần cập nhật từ cửa hàng.", ""));
    }

    public static synchronized void cache(ShopProduct p) {
        PRODUCTS.put(p.id, p);
    }

    public static synchronized ShopProduct get(int id) {
        return PRODUCTS.get(id);
    }

    public static synchronized List<ShopProduct> real() {
        List<ShopProduct> r = new ArrayList<>();
        for (ShopProduct p : PRODUCTS.values()) {
            if (!p.isSample()) r.add(p);
        }
        return r;
    }

    public static List<ShopProduct> samples() {
        List<ShopProduct> r = new ArrayList<>();
        for (int id : new int[]{-1, -2, -3, -4, -5, -6, -7, -8, -9, -10}) {
            r.add(get(id));
        }
        return r;
    }

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static String stripAccents(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        String stripped = DIACRITICS.matcher(normalized).replaceAll("");
        return stripped.replace('đ', 'd').replace('Đ', 'D');
    }

    public static boolean matchesSearch(String name, String query) {
        if (query == null || query.trim().isEmpty()) return true;
        if (name == null || name.trim().isEmpty()) return false;

        String cleanText = stripAccents(name).toLowerCase(Locale.ROOT).trim();
        String cleanQuery = stripAccents(query).toLowerCase(Locale.ROOT).trim();

        // 1. Exact or accent-stripped substring match
        if (cleanText.contains(cleanQuery)) return true;

        // 2. Space-insensitive match (e.g. "aokhoac" matching "ao khoac")
        String textKey = cleanText.replaceAll("[^a-z0-9]", "");
        String queryKey = cleanQuery.replaceAll("[^a-z0-9]", "");
        if (!queryKey.isEmpty() && textKey.contains(queryKey)) return true;

        // 3. Word-token match (all words in query exist in product name)
        String[] words = cleanQuery.split("\\s+");
        boolean allWordsMatch = true;
        for (String w : words) {
            if (!w.isEmpty() && !cleanText.contains(w) && !textKey.contains(w)) {
                allWordsMatch = false;
                break;
            }
        }
        return allWordsMatch && words.length > 0;
    }

    public static List<ShopProduct> filter(List<ShopProduct> source, String category, String query) {
        List<ShopProduct> r = new ArrayList<>();
        for (ShopProduct p : source) {
            boolean matchCategory = category.isEmpty() || category.equalsIgnoreCase(p.category);
            boolean matchQuery = matchesSearch(p.name, query);
            if (matchCategory && matchQuery) {
                r.add(p);
            }
        }
        if ("Áo".equalsIgnoreCase(category)) {
            r.sort(Comparator.comparingInt(p -> {
                int[] ids = {-3, -5, -7, -8, -9, -10, -1, -2};
                for (int i = 0; i < ids.length; i++) {
                    if (ids[i] == p.id) return i;
                }
                return 99;
            }));
        }
        return r;
    }
}
