package com.example.ecom.core.api;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 1 file (thường là ảnh) gửi kèm request multipart.
 * ≈ formData.append("avatar", file) trong JavaScript.
 *
 * Ví dụ lấy ảnh người dùng vừa chọn trong thư viện:
 *   FilePart avatar = FilePart.fromUri(context, "avatar", uri);
 */
public class FilePart {

    /** Tên field backend nhận, vd. "avatar", "images". */
    public final String field;
    public final String fileName;
    public final String mimeType;
    public final byte[] data;

    public FilePart(String field, String fileName, String mimeType, byte[] data) {
        this.field = field;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.data = data;
    }

    /** Đọc file từ Uri (ảnh chọn từ thư viện / camera). Nên gọi ngoài main thread nếu file lớn. */
    public static FilePart fromUri(Context context, String field, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        String mimeType = resolver.getType(uri);
        if (mimeType == null) mimeType = "application/octet-stream";

        String fileName = "file";
        try (Cursor c = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) fileName = c.getString(0);
        }

        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) throw new IOException("Không đọc được file: " + uri);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) != -1) buffer.write(chunk, 0, n);
            return new FilePart(field, fileName, mimeType, buffer.toByteArray());
        }
    }
}
