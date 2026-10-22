package com.dangkymonhoc.controller;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;

/** Tạo chuỗi "redirect:" quay về trang trước (giữ nguyên bộ lọc, trang hiện tại). */
public final class Redirects {

    private Redirects() {
    }

    /** Quay về trang trong header Referer nếu cùng host, ngược lại về {@code fallback}. */
    public static String back(HttpServletRequest request, String fallback) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                String ctx = request.getContextPath();
                String path = uri.getRawPath();
                boolean cungHost = uri.getHost() == null || uri.getHost().equalsIgnoreCase(request.getServerName());
                if (cungHost && path != null && path.startsWith(ctx + "/")) {
                    String query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
                    return "redirect:" + path.substring(ctx.length()) + query;
                }
            } catch (IllegalArgumentException ignored) {
                // Referer không hợp lệ: dùng fallback
            }
        }
        return "redirect:" + fallback;
    }
}
