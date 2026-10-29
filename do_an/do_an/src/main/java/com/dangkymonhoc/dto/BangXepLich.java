package com.dangkymonhoc.dto;

import java.util.List;

/**
 * Toàn bộ lịch của một học kỳ cho màn hình MH20.
 *
 * @param trangThai TRONG (chưa có buổi nào), NHAP (còn buổi nháp) hoặc CHINH_THUC
 */
public record BangXepLich(List<BuoiHocRow> buoi, List<ChuaXepRow> chuaXep, String trangThai,
                          int soPhanCong, int soNhap, int soXungDot) {

    public static final String TRONG = "TRONG";

    /** Chỉ lưu chính thức khi còn buổi nháp, không còn xung đột và mọi phân công đã có lịch. */
    public boolean coTheChinhThuc() {
        return soNhap > 0 && soXungDot == 0 && chuaXep.isEmpty();
    }
}
