package com.dangkymonhoc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Một dòng ở danh sách khoa (MH13). */
@Getter
@AllArgsConstructor
public class KhoaRow {
    private String maKhoa;
    private String tenKhoa;
    private Long soLop;
    private Long soGiangVien;
    private Long soMonHoc;
}
