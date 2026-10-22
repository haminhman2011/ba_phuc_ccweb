package com.dangkymonhoc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Môn và số sinh viên đăng ký, dùng ở trang tổng quan (MH12). */
@Getter
@AllArgsConstructor
public class TopMonRow {
    private String maMH;
    private String tenMH;
    private Long soSinhVien;
}
