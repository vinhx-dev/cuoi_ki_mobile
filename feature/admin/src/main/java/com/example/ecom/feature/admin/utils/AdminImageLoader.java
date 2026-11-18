package com.example.ecom.feature.admin.utils;
import android.content.Context;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.*;
/** Downsamples user photos off the main thread and avoids assigning recycled rows. */
public final class AdminImageLoader {
    private static final ExecutorService POOL=Executors.newFixedThreadPool(2);
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final LruCache<String,Bitmap> CACHE=new LruCache<String,Bitmap>(8*1024*1024) {
        @Override protected int sizeOf(String k,Bitmap b) {
            return b.getAllocationByteCount();
        }
    }
    ;
    private AdminImageLoader() {
    }
    public static void load(ImageView view,String uri,int placeholder) {
        view.setTag(uri);
        view.setImageResource(placeholder);
        Bitmap cached=CACHE.get(uri);
        if(cached!=null) {
            view.setImageBitmap(cached);
            return;
        }
        WeakReference<ImageView> target=new WeakReference<>(view);
        Context context=view.getContext().getApplicationContext();
        POOL.execute(()-> {
            try {
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inJustDecodeBounds=true;
                try(InputStream stream=context.getContentResolver().openInputStream(Uri.parse(uri))) {
                    BitmapFactory.decodeStream(stream,null,options);
                }
                options.inSampleSize=1;
                while(options.outWidth/options.inSampleSize>720||options.outHeight/options.inSampleSize>720)options.inSampleSize*=2;
                options.inJustDecodeBounds=false;
                Bitmap result;
                try(InputStream stream=context.getContentResolver().openInputStream(Uri.parse(uri))) {
                    result=BitmapFactory.decodeStream(stream,null,options);
                }
                if(result!=null) {
                    CACHE.put(uri,result);
                    MAIN.post(()-> {
                        ImageView current=target.get();
                        if(current!=null&&uri.equals(current.getTag()))current.setImageBitmap(result);
                    }
                    );
                }
            }
            catch(Exception ignored) {
                /* Keep bundled product art when a URI is no longer available. */
            }
        }
        );
    }
}
