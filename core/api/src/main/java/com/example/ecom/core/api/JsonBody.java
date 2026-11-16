package com.example.ecom.core.api;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Body dạng JSON. ≈ fetch(url, { body: JSON.stringify(data) }) */
public class JsonBody implements RequestBody {

    private final JSONObject json;

    public JsonBody(JSONObject json) {
        this.json = json;
    }

    @Override
    public String contentType() {
        return "application/json; charset=utf-8";
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(json.toString().getBytes(StandardCharsets.UTF_8));
    }
}
