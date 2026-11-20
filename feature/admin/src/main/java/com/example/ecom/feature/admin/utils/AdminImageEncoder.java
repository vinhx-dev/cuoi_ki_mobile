package com.example.ecom.feature.admin.utils;

import android.content.Context;
import android.graphics.*;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.*;
import android.util.Base64;
import java.io.*;
import java.util.concurrent.*;

/** Reads the selected document locally; only the encoded image is sent to the API. */
public final class AdminImageEncoder {
    private static final ExecutorService POOL=Executors.newSingleThreadExecutor();
    private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private static final int MAX_BYTES=2*1024*1024, MAX_EDGE=1600;
    public interface Callback { void complete(String image,String error); }
    private AdminImageEncoder() {}

    public static void encode(Context context,Uri uri,Callback callback) {
        Context app=context.getApplicationContext();
        POOL.execute(() -> {
            try {
                String result=read(app,uri);
                MAIN.post(() -> callback.complete(result,null));
            } catch(Exception e) {
                MAIN.post(() -> callback.complete(null,"Không đọc được ảnh. Hãy chọn lại ảnh JPEG/PNG từ máy."));
            }
        });
    }

    private static String read(Context context,Uri uri) throws IOException {
        BitmapFactory.Options options=new BitmapFactory.Options(); options.inJustDecodeBounds=true;
        try(InputStream stream=open(context,uri)) { BitmapFactory.decodeStream(stream,null,options); }
        if(options.outWidth<1 || options.outHeight<1) throw new IOException("Invalid image");
        options.inSampleSize=1;
        while(options.outWidth>(long)MAX_EDGE*options.inSampleSize || options.outHeight>(long)MAX_EDGE*options.inSampleSize)
            options.inSampleSize*=2;
        options.inJustDecodeBounds=false;
        Bitmap bitmap;
        try(InputStream stream=open(context,uri)) { bitmap=BitmapFactory.decodeStream(stream,null,options); }
        if(bitmap==null) throw new IOException("Invalid image");
        try {
            Matrix matrix=orientation(context,uri);
            if(!matrix.isIdentity()) {
                Bitmap rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
                if(rotated!=bitmap) { bitmap.recycle(); bitmap=rotated; }
            }
            boolean transparent=bitmap.hasAlpha();
            while(true) {
                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                if(!bitmap.compress(transparent?Bitmap.CompressFormat.PNG:Bitmap.CompressFormat.JPEG,85,bytes))
                    throw new IOException("Cannot encode image");
                if(bytes.size()<=MAX_BYTES) return "data:image/"+(transparent?"png":"jpeg")+";base64,"
                    +Base64.encodeToString(bytes.toByteArray(),Base64.NO_WRAP);
                if(Math.max(bitmap.getWidth(),bitmap.getHeight())<=64) throw new IOException("Image too large");
                Bitmap smaller=Bitmap.createScaledBitmap(bitmap,Math.max(1,bitmap.getWidth()*3/4),
                    Math.max(1,bitmap.getHeight()*3/4),true);
                bitmap.recycle(); bitmap=smaller;
            }
        } finally { bitmap.recycle(); }
    }

    private static InputStream open(Context context,Uri uri) throws IOException {
        InputStream input=context.getContentResolver().openInputStream(uri);
        if(input==null) throw new IOException("Missing image");
        return input;
    }

    private static Matrix orientation(Context context,Uri uri) {
        Matrix matrix=new Matrix();
        try(InputStream input=open(context,uri)) {
            int value=new ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch(value) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL: matrix.setScale(-1,1); break;
                case ExifInterface.ORIENTATION_ROTATE_180: matrix.setRotate(180); break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL: matrix.setScale(1,-1); break;
                case ExifInterface.ORIENTATION_TRANSPOSE: matrix.setRotate(90); matrix.postScale(-1,1); break;
                case ExifInterface.ORIENTATION_ROTATE_90: matrix.setRotate(90); break;
                case ExifInterface.ORIENTATION_TRANSVERSE: matrix.setRotate(-90); matrix.postScale(-1,1); break;
                case ExifInterface.ORIENTATION_ROTATE_270: matrix.setRotate(-90); break;
                default: break;
            }
        } catch(IOException ignored) { /* Formats without EXIF use their decoded orientation. */ }
        return matrix;
    }
}
