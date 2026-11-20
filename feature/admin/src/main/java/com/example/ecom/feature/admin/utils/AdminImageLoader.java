package com.example.ecom.feature.admin.utils;

import android.widget.ImageView;
import com.example.ecom.core.ui.image.InlineImageLoader;

public final class AdminImageLoader {
    private AdminImageLoader() {}
    public static void load(ImageView view,String image,int placeholder) {
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        InlineImageLoader.load(view,image,placeholder);
    }
}
