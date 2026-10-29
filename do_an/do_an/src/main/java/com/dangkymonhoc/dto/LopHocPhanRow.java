package com.dangkymonhoc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Một lớp (giảng viên) của môn được mở, sinh viên chọn lớp để đăng ký (MH07). */
@Getter
@AllArgsConstructor
public class LopHocPhanRow {
    private String maGV;
    private String tenGV;
    /** Ví dụ "Thứ 3, tiết 1–4, P.A101"; null khi lớp chưa có lịch chính thức. */
    private String lich;
    private long siSo;
    private int sucChua;
    /** Sinh viên đang học lớp này. */
    private boolean daDangKy;
    /** QĐ10: thông báo trùng lịch với môn đã đăng ký, null nếu không trùng. */
    private String trungLich;

    public boolean isCoLich() {
        return lich != null;
    }

    public boolean isDay() {
        return lich != null && siSo >= sucChua;
    }
}
