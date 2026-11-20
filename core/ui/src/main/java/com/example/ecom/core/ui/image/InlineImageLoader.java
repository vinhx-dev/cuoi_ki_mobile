package com.example.ecom.core.ui.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.*;
import java.lang.ref.WeakReference;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.*;

/** Decodes API images off the UI thread. Older public URLs never receive app credentials. */
public final class InlineImageLoader {
    private static final int INLINE_LIMIT=2*1024*1024, URL_LIMIT=8*1024*1024;
    private static final ExecutorService POOL=Executors.newFixedThreadPool(2);
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final LruCache<String,Bitmap> CACHE=new LruCache<String,Bitmap>(8*1024*1024) {
        @Override protected int sizeOf(String key,Bitmap image) { return image.getAllocationByteCount(); }
    };
    private InlineImageLoader() {}

    public static void load(ImageView view,String source,int placeholder) {
        Object request=new Object();
        view.setTag(request); view.setImageResource(placeholder);
        if(source==null || source.isEmpty()) return;
        WeakReference<ImageView> target=new WeakReference<>(view);
        POOL.execute(() -> {
            try {
                String key=key(source);
                Bitmap bitmap=CACHE.get(key);
                if(bitmap==null) {
                    bitmap=decode(read(source));
                    if(bitmap==null) return;
                    CACHE.put(key,bitmap);
                }
                Bitmap result=bitmap;
                MAIN.post(() -> {
                    ImageView current=target.get();
                    if(current!=null && current.getTag()==request) current.setImageBitmap(result);
                });
            } catch(Exception ignored) {
                // A missing or invalid image keeps the existing placeholder.
            }
        });
    }

    private static String key(String value) throws Exception {
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(hash,Base64.NO_WRAP);
    }

    private static byte[] read(String source) throws IOException {
        if(!source.startsWith("http://") && !source.startsWith("https://")) {
            if(source.length()>((INLINE_LIMIT+2)/3)*4+64) throw new IOException("Image too large");
            String encoded=source;
            if(source.regionMatches(true,0,"data:",0,5)) {
                int comma=source.indexOf(',');
                if(comma<0) throw new IOException("Invalid image data URI");
                String header=source.substring(0,comma).toLowerCase(java.util.Locale.ROOT);
                if(!header.equals("data:image/jpeg;base64") && !header.equals("data:image/jpg;base64")
                        && !header.equals("data:image/png;base64")) throw new IOException("Unsupported image");
                encoded=source.substring(comma+1);
            }
            byte[] bytes=Base64.decode(encoded,Base64.DEFAULT);
            if(bytes.length>INLINE_LIMIT) throw new IOException("Image too large");
            return bytes;
        }
        HttpURLConnection connection=(HttpURLConnection)new URL(source).openConnection();
        try {
            connection.setConnectTimeout(10000); connection.setReadTimeout(10000);
            if(connection.getResponseCode()!=200) throw new IOException("Image unavailable");
            try(InputStream input=connection.getInputStream(); ByteArrayOutputStream output=new ByteArrayOutputStream()) {
                byte[] buffer=new byte[8192]; int count;
                while((count=input.read(buffer))!=-1) {
                    if(output.size()+count>URL_LIMIT) throw new IOException("Image too large");
                    output.write(buffer,0,count);
                }
                return output.toByteArray();
            }
        } finally { connection.disconnect(); }
    }

    private static Bitmap decode(byte[] bytes) {
        BitmapFactory.Options options=new BitmapFactory.Options(); options.inJustDecodeBounds=true;
        BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
        if(options.outWidth<1 || options.outHeight<1) return null;
        options.inSampleSize=1;
        while(options.outWidth/options.inSampleSize>720 || options.outHeight/options.inSampleSize>720)
            options.inSampleSize*=2;
        options.inJustDecodeBounds=false;
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
    }
}
