package com.dangkymonhoc.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Phân công chưa có lịch, kèm lý do không xếp được (khu "Chưa xếp được" của MH20). */
@Getter
@AllArgsConstructor
public class ChuaXepRow {
    private String maGV;
    private String tenGV;
    private String maMH;
    private String tenMH;
    private int soTiet;
    private long siSo;
    private String lyDo;

    public String getTieuDe() {
        return maMH + " – " + tenMH + " · " + tenGV + " · " + soTiet + " tiết · " + siSo + " SV";
    }
}
