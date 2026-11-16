package com.example.ecom.core.api;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Body dạng form có kèm file/ảnh (multipart/form-data).
 * ≈ const form = new FormData(); form.append("name", "..."); form.append("avatar", file);
 */
public class MultipartBody implements RequestBody {

    private final String boundary = "----EcomBoundary" + UUID.randomUUID().toString().replace("-", "");
    private final Map<String, String> fields;
    private final List<FilePart> files;

    /**
     * @param fields các trường chữ (có thể null)
     * @param files  các file/ảnh (có thể null)
     */
    public MultipartBody(Map<String, String> fields, List<FilePart> files) {
        this.fields = fields != null ? fields : Collections.emptyMap();
        this.files = files != null ? files : Collections.emptyList();
    }

    @Override
    public String contentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        for (Map.Entry<String, String> field : fields.entrySet()) {
            write(out, "--" + boundary + "\r\n");
            write(out, "Content-Disposition: form-data; name=\"" + field.getKey() + "\"\r\n\r\n");
            write(out, field.getValue() + "\r\n");
        }
        for (FilePart file : files) {
            write(out, "--" + boundary + "\r\n");
            write(out, "Content-Disposition: form-data; name=\"" + file.field
                    + "\"; filename=\"" + file.fileName + "\"\r\n");
            write(out, "Content-Type: " + file.mimeType + "\r\n\r\n");
            out.write(file.data);
            write(out, "\r\n");
        }
        write(out, "--" + boundary + "--\r\n");
    }

    private static void write(OutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
    }
}
