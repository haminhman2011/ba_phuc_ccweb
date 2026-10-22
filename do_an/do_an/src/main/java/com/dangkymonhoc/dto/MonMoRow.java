package com.dangkymonhoc.dto;

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
    /** Họ tên các giảng viên được phân công, nối bằng dấu phẩy. */
    private String giangVien;
    private boolean daDangKy;
    /** Đăng ký thêm môn này sẽ vượt số tín chỉ tối đa. */
    private boolean vuotTinChi;
}
