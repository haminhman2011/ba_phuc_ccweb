package com.dangkymonhoc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Một dòng ở danh sách lớp (MH14). */
@Getter
@AllArgsConstructor
public class LopRow {
    private String maLop;
    private String tenLop;
    private String khoaHoc;
    private String maKhoa;
    private String tenKhoa;
    private Long siSo;
}
