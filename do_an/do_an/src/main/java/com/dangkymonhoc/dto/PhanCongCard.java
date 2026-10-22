package com.dangkymonhoc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Thẻ môn được phân công của giảng viên (MH10). */
@Getter
@AllArgsConstructor
public class PhanCongCard {
    private String maMH;
    private String tenMH;
    private int soTinChi;
    private long siSo;
    private long daNhapDiem;

    public int getPhanTram() {
        return siSo == 0 ? 0 : (int) Math.round(daNhapDiem * 100.0 / siSo);
    }
}
