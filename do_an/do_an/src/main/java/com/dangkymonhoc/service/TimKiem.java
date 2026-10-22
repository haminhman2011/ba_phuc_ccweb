package com.dangkymonhoc.service;

/** Chuẩn hóa tham số tìm kiếm/lọc trước khi đưa vào JPQL. */
final class TimKiem {

    private TimKiem() {
    }

    /** "  Abc " → "%abc%"; null → "%%" (khớp tất cả). */
    static String like(String q) {
        return "%" + (q == null ? "" : q.trim().toLowerCase()) + "%";
    }

    /** null → "" để câu truy vấn hiểu là "không lọc". */
    static String loc(String value) {
        return value == null ? "" : value.trim();
    }

    /** Chuẩn hóa mã: bỏ khoảng trắng, viết hoa. */
    static String ma(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
