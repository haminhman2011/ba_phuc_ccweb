package com.manha.lab0910.form;

import com.manha.lab0910.entity.PhieuXuat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * (UC4) Dữ liệu bước 1 của màn hình xuất kho.
 *
 * Người dùng chỉ khai "muốn xuất bao nhiêu của sản phẩm nào", còn trừ ở lô nào
 * là do hệ thống tự tính theo FEFO chứ không cho chọn tay.
 */
public class PhieuXuatForm {

    @NotNull(message = "Vui lòng chọn sản phẩm")
    private Long sanPhamId;

    @NotNull(message = "Số lượng xuất phải lớn hơn 0")
    @Min(value = 1, message = "Số lượng xuất phải lớn hơn 0")
    private Integer soLuong;

    @NotNull(message = "Vui lòng chọn loại xuất")
    private PhieuXuat.LoaiXuat loaiXuat = PhieuXuat.LoaiXuat.XUAT_BAN;

    @Size(max = 255, message = "Lý do tối đa 255 ký tự")
    private String lyDo;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String ghiChu;

    public Long getSanPhamId() { return sanPhamId; }
    public void setSanPhamId(Long sanPhamId) { this.sanPhamId = sanPhamId; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public PhieuXuat.LoaiXuat getLoaiXuat() { return loaiXuat; }
    public void setLoaiXuat(PhieuXuat.LoaiXuat loaiXuat) { this.loaiXuat = loaiXuat; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
