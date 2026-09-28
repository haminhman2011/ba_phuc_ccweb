package com.manha.lab0910.form;

/** Một dòng của bảng báo cáo Nhập - Xuất - Tồn. */
public class DongBaoCaoTon {

    private String maSanPham;
    private String tenSanPham;
    private String donViTinh;
    private int tonDau;
    private int nhapTrongKy;
    private int xuatTrongKy;
    private int tonCuoi;

    public DongBaoCaoTon(String maSanPham, String tenSanPham, String donViTinh,
                         int tonDau, int nhapTrongKy, int xuatTrongKy, int tonCuoi) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
        this.tonDau = tonDau;
        this.nhapTrongKy = nhapTrongKy;
        this.xuatTrongKy = xuatTrongKy;
        this.tonCuoi = tonCuoi;
    }

    public String getMaSanPham() { return maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public String getDonViTinh() { return donViTinh; }
    public int getTonDau() { return tonDau; }
    public int getNhapTrongKy() { return nhapTrongKy; }
    public int getXuatTrongKy() { return xuatTrongKy; }
    public int getTonCuoi() { return tonCuoi; }
}
