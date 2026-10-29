package com.dangkymonhoc.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Một môn được mở trong học kỳ, hiển thị ở khối 1 màn hình đăng ký (MH07). */
@Getter
@AllArgsConstructor
public class MonMoRow {
    private String maMH;
    private String tenMH;
    private int soTinChi;
    private String tenKhoa;
    private boolean daDangKy;
    /** Đăng ký thêm môn này sẽ vượt số tín chỉ tối đa. */
    private boolean vuotTinChi;
    /** Các lớp của môn (mỗi giảng viên được phân công một lớp). */
    private List<LopHocPhanRow> lops;
}
