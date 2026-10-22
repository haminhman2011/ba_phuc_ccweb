package com.dangkymonhoc.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Một dòng ở bảng nhập điểm (MH11). */
@Getter
@AllArgsConstructor
public class NhapDiemRow {
    private String maSV;
    private String hoTen;
    private String tenLop;
    private LocalDate ngayDangKy;
    /** Giá trị đang hiển thị trong ô điểm (giữ nguyên chuỗi người dùng gõ khi có lỗi). */
    private String diem;
    private boolean loi;
}
