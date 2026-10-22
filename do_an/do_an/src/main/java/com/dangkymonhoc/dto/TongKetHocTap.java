package com.dangkymonhoc.dto;

import java.util.List;

import com.dangkymonhoc.entity.DangKy;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Thẻ tổng kết ở màn hình kết quả học tập (MH08). */
@Getter
@AllArgsConstructor
public class TongKetHocTap {
    private int tongTinChi;
    private int tinChiDat;
    /** Trung bình có trọng số theo tín chỉ, chỉ tính môn đã có điểm; null nếu chưa có môn nào có điểm. */
    private Double diemTrungBinh;

    public static TongKetHocTap tinh(List<DangKy> dangKys) {
        int tongTinChi = 0;
        int tinChiDat = 0;
        int tinChiCoDiem = 0;
        double tongDiem = 0;
        for (DangKy dk : dangKys) {
            int tc = dk.getMonHoc().getSoTinChi();
            tongTinChi += tc;
            if (dk.getDiem() != null) {
                tinChiCoDiem += tc;
                tongDiem += dk.getDiem() * tc;
                if (dk.getDiem() >= 5) {
                    tinChiDat += tc;
                }
            }
        }
        Double dtb = tinChiCoDiem == 0 ? null : Math.round(tongDiem / tinChiCoDiem * 100) / 100.0;
        return new TongKetHocTap(tongTinChi, tinChiDat, dtb);
    }
}
