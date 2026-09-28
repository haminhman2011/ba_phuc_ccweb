package com.manha.lab0910.form;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Một dòng của bảng báo cáo Hàng huỷ (hàng xuất với loại XUAT_HUY). */
public class DongBaoCaoHuy {

    private String maLo;
    private String tenSanPham;
    private int soLuongHuy;
    private LocalDate hanSuDung;
    private LocalDateTime ngayHuy;
    private String lyDo;

    public DongBaoCaoHuy(String maLo, String tenSanPham, int soLuongHuy,
                         LocalDate hanSuDung, LocalDateTime ngayHuy, String lyDo) {
        this.maLo = maLo;
        this.tenSanPham = tenSanPham;
        this.soLuongHuy = soLuongHuy;
        this.hanSuDung = hanSuDung;
        this.ngayHuy = ngayHuy;
        this.lyDo = lyDo;
    }

    public String getMaLo() { return maLo; }
    public String getTenSanPham() { return tenSanPham; }
    public int getSoLuongHuy() { return soLuongHuy; }
    public LocalDate getHanSuDung() { return hanSuDung; }
    public LocalDateTime getNgayHuy() { return ngayHuy; }
    public String getLyDo() { return lyDo; }
}
