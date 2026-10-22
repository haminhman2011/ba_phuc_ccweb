package com.dangkymonhoc.dto;

import java.util.List;

import com.dangkymonhoc.entity.DangKy;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Các môn đã đăng ký của một học kỳ, dùng ở màn hình kết quả học tập (MH08). */
@Getter
@AllArgsConstructor
public class KetQuaNhom {
    private String hocKy;
    private List<DangKy> dangKys;
}
