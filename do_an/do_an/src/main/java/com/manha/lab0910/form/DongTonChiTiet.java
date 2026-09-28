package com.manha.lab0910.form;

import java.time.LocalDate;

/** Một dòng của bảng tồn kho ở chế độ xem chi tiết theo lô. */
public class DongTonChiTiet {

    private String maLo;
    private String tenSanPham;
    private String tenViTri;
    private LocalDate hanSuDung;
    private int soLuongTon;
    private long soNgayConLai;

    public DongTonChiTiet(String maLo, String tenSanPham, String tenViTri,
                          LocalDate hanSuDung, int soLuongTon, long soNgayConLai) {
        this.maLo = maLo;
        this.tenSanPham = tenSanPham;
        this.tenViTri = tenViTri;
        this.hanSuDung = hanSuDung;
        this.soLuongTon = soLuongTon;
        this.soNgayConLai = soNgayConLai;
    }

    public String getMaLo() { return maLo; }
    public String getTenSanPham() { return tenSanPham; }
    public String getTenViTri() { return tenViTri; }
    public LocalDate getHanSuDung() { return hanSuDung; }
    public int getSoLuongTon() { return soLuongTon; }
    public long getSoNgayConLai() { return soNgayConLai; }

    // Quá hạn thì tô đỏ, sắp hết hạn thì tô cam (đặc tả 5.7)
    public boolean isQuaHan() {
        return soNgayConLai < 0;
    }

    public boolean isSapHetHan() {
        return soNgayConLai >= 0 && soNgayConLai <= 7;
    }
}
