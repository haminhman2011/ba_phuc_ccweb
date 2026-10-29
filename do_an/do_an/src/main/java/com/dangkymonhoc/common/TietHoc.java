package com.dangkymonhoc.common;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Quy ước tiết học, gán cứng theo quy định đào tạo:
 * <ul>
 * <li>một ngày 10 tiết, sáng tiết 1–5, chiều tiết 6–10, mỗi tiết 50 phút;</li>
 * <li>một tín chỉ = 15 tiết lý thuyết, nên một môn học đủ số tuần để đạt 15 × số tín chỉ tiết.</li>
 * </ul>
 */
public final class TietHoc {

    public static final int SO_TIET_MOT_BUOI = 5;
    public static final int SO_TIET_MOT_NGAY = 2 * SO_TIET_MOT_BUOI;
    public static final int SO_PHUT_MOT_TIET = 50;
    public static final int SO_TIET_MOT_TIN_CHI = 15;

    /** Giờ bắt đầu tiết 1–10, có nghỉ 10 phút sau tiết 2, 4, 7, 9. */
    private static final LocalTime[] GIO_BAT_DAU = {
            LocalTime.of(7, 0), LocalTime.of(7, 50), LocalTime.of(8, 50), LocalTime.of(9, 40), LocalTime.of(10, 40),
            LocalTime.of(13, 0), LocalTime.of(13, 50), LocalTime.of(14, 50), LocalTime.of(15, 40), LocalTime.of(16, 40)
    };

    private TietHoc() {
    }

    public static LocalTime batDau(int tiet) {
        return GIO_BAT_DAU[tiet - 1];
    }

    public static LocalTime ketThuc(int tiet) {
        return batDau(tiet).plusMinutes(SO_PHUT_MOT_TIET);
    }

    /** Ví dụ tiết 1–3 → "07:00–09:40". */
    public static String khungGio(int tietBatDau, int tietKetThuc) {
        return batDau(tietBatDau) + "–" + ketThuc(tietKetThuc);
    }

    /** Giờ bắt đầu của 10 tiết dạng "07:00", để vẽ cột tiết trên lưới tuần. */
    public static List<String> dsGioBatDau() {
        List<String> ds = new ArrayList<>();
        for (LocalTime gio : GIO_BAT_DAU) {
            ds.add(gio.toString());
        }
        return ds;
    }

    /** Số tuần học để đủ 15 × số tín chỉ tiết khi mỗi tuần học một buổi {@code soTietMoiBuoi} tiết. */
    public static int soTuanCan(int soTinChi, int soTietMoiBuoi) {
        int tongTiet = soTinChi * SO_TIET_MOT_TIN_CHI;
        return (tongTiet + soTietMoiBuoi - 1) / soTietMoiBuoi;
    }
}
