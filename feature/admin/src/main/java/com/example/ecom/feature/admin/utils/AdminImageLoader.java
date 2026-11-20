package com.example.ecom.feature.admin.utils;
import android.graphics.*;
import android.os.*;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.*;
import java.util.concurrent.*;

/** Public image URLs are loaded without sending the app's bearer token to image hosts. */
public final class AdminImageLoader {
    private static final ExecutorService POOL=Executors.newFixedThreadPool(2);
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final LruCache<String,Bitmap> CACHE=new LruCache<String,Bitmap>(8*1024*1024) {
        @Override protected int sizeOf(String key,Bitmap bitmap) { return bitmap.getAllocationByteCount(); }
    };
    private AdminImageLoader() {}
    public static void load(ImageView view,String url,int placeholder) {
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setTag(url); view.setImageResource(placeholder);
        if(url==null || !(url.startsWith("http://")||url.startsWith("https://"))) return;
        Bitmap cached=CACHE.get(url);
        if(cached!=null) { view.setImageBitmap(cached); return; }
        WeakReference<ImageView> target=new WeakReference<>(view);
        POOL.execute(() -> {
            HttpURLConnection connection=null;
            try {
                connection=(HttpURLConnection)new URL(url).openConnection();
                connection.setConnectTimeout(10000); connection.setReadTimeout(10000);
                if(connection.getResponseCode()!=200) return;
                byte[] bytes;
                try(InputStream input=connection.getInputStream(); ByteArrayOutputStream output=new ByteArrayOutputStream()) {
                    byte[] buffer=new byte[8192]; int read;
                    while((read=input.read(buffer))!=-1) {
                        if(output.size()+read>8*1024*1024) return;
                        output.write(buffer,0,read);
                    }
                    bytes=output.toByteArray();
                }
                BitmapFactory.Options options=new BitmapFactory.Options(); options.inJustDecodeBounds=true;
                BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
                options.inSampleSize=1;
                while(options.outWidth/options.inSampleSize>720 || options.outHeight/options.inSampleSize>720) options.inSampleSize*=2;
                options.inJustDecodeBounds=false;
                Bitmap result=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
                if(result==null) return;
                CACHE.put(url,result);
                MAIN.post(() -> {
                    ImageView current=target.get();
                    if(current!=null && url.equals(current.getTag())) current.setImageBitmap(result);
                });
            } catch(Exception ignored) {
                // Keep a neutral image placeholder if this URL is unavailable.
            } finally { if(connection!=null) connection.disconnect(); }
        });
    }
}
