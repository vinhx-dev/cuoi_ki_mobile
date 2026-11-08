package com.example.myapplication.frontend.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Formatter {

    private Formatter() {}

    private static final Locale VI = Locale.forLanguageTag("vi-VN");

    public static String money(long amount) {
        return NumberFormat.getNumberInstance(VI).format(amount) + " ₫";
    }

    public static String dateTime(long millis) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", VI).format(new Date(millis));
    }

    public static String orderStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "Chờ xử lý";
            case "SHIPPING": return "Đang giao";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
}
