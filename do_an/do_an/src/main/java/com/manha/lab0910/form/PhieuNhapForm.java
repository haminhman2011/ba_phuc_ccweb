package com.manha.lab0910.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * (UC3) Dữ liệu của cả màn hình Nhập kho: phần đầu phiếu + danh sách dòng hàng.
 *
 * Không bind thẳng vào entity PhieuNhap được, vì một dòng trên màn hình
 * trải ra 2 bảng khác nhau: thông tin lô (mã lô, hạn dùng, vị trí) nằm ở lo_hang,
 * còn số lượng và đơn giá nằm ở chi_tiet_nhap.
 */
public class PhieuNhapForm {

    @Size(max = 150, message = "Nhà cung cấp tối đa 150 ký tự")
    private String nhaCungCap;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String ghiChu;

    // (luồng phụ 4c) Người dùng đã bấm "Xác nhận vẫn nhập" cho hàng sắp/đã hết hạn chưa
    private boolean xacNhanHetHan;

    // @Valid để các ràng buộc bên trong từng dòng cũng được kiểm tra
    @Valid
    private List<DongNhapForm> chiTiet = new ArrayList<>();

    public String getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(String nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public boolean isXacNhanHetHan() { return xacNhanHetHan; }
    public void setXacNhanHetHan(boolean xacNhanHetHan) { this.xacNhanHetHan = xacNhanHetHan; }
    public List<DongNhapForm> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<DongNhapForm> chiTiet) { this.chiTiet = chiTiet; }
}
