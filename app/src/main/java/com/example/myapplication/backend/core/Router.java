package com.example.myapplication.backend.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Router đơn giản: map (method + path pattern) -> handler.
 * Hỗ trợ path param dạng "/products/{id}".
 */
public class Router {

    private static class Route {
        final HttpMethod method;
        final String[] segments;
        final RouteHandler handler;

        Route(HttpMethod method, String pattern, RouteHandler handler) {
            this.method = method;
            this.segments = split(pattern);
            this.handler = handler;
        }
    }

    private final List<Route> routes = new ArrayList<>();

    public Router get(String pattern, RouteHandler h) { return add(HttpMethod.GET, pattern, h); }
    public Router post(String pattern, RouteHandler h) { return add(HttpMethod.POST, pattern, h); }
    public Router put(String pattern, RouteHandler h) { return add(HttpMethod.PUT, pattern, h); }
    public Router delete(String pattern, RouteHandler h) { return add(HttpMethod.DELETE, pattern, h); }

    public Router add(HttpMethod method, String pattern, RouteHandler handler) {
        routes.add(new Route(method, pattern, handler));
        return this;
    }

    public ApiResponse dispatch(ApiRequest request) throws Exception {
        String[] reqSegments = split(request.getPath());
        for (Route route : routes) {
            if (route.method != request.getMethod()) continue;
            Map<String, String> params = match(route.segments, reqSegments);
            if (params != null) {
                request.putPathParams(params);
                return route.handler.handle(request);
            }
        }
        throw ApiException.notFound("Không tìm thấy API: " + request.getMethod() + " " + request.getPath());
    }

    private static Map<String, String> match(String[] pattern, String[] actual) {
        if (pattern.length != actual.length) return null;
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < pattern.length; i++) {
            String p = pattern[i];
            if (p.startsWith("{") && p.endsWith("}")) {
                params.put(p.substring(1, p.length() - 1), actual[i]);
            } else if (!p.equals(actual[i])) {
                return null;
            }
        }
        return params;
    }

    private static String[] split(String path) {
        String clean = path.split("\\?")[0].replaceAll("^/+|/+$", "");
        return clean.isEmpty() ? new String[0] : clean.split("/");
    }
}
