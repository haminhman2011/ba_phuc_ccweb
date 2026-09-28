package com.manha.lab0910.form;

/** Một dòng của bảng tồn kho ở chế độ xem tổng hợp theo sản phẩm. */
public class DongTonTongHop {

    private String maSanPham;
    private String tenSanPham;
    private String donViTinh;
    private int tongTon;
    private int tonToiThieu;

    public DongTonTongHop(String maSanPham, String tenSanPham, String donViTinh,
                          int tongTon, int tonToiThieu) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
        this.tongTon = tongTon;
        this.tonToiThieu = tonToiThieu;
    }

    public String getMaSanPham() { return maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public String getDonViTinh() { return donViTinh; }
    public int getTongTon() { return tongTon; }
    public int getTonToiThieu() { return tonToiThieu; }

    // Dùng để hiện dấu cảnh báo trên màn hình
    public boolean isDuoiNguong() {
        return tongTon < tonToiThieu;
    }
}
