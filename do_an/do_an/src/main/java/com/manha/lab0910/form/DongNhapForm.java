package com.manha.lab0910.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Một dòng hàng trong phiếu nhập.
 * Mỗi dòng sẽ đẻ ra 1 bản ghi lo_hang + 1 bản ghi chi_tiet_nhap khi lưu.
 */
public class DongNhapForm {

    @NotNull(message = "Vui lòng chọn sản phẩm")
    private Long sanPhamId;

    @NotBlank(message = "Vui lòng nhập mã lô")
    @Size(max = 30, message = "Mã lô tối đa 30 ký tự")
    private String maLo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngaySanXuat;

    @NotNull(message = "Vui lòng nhập hạn dùng")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hanSuDung;

    // Vị trí để trống được, hàng chưa xếp kệ vẫn nhập kho bình thường
    private Long viTriId;

    @NotNull(message = "Số lượng phải lớn hơn 0")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    // Để trống cũng được; nếu có nhập thì không được âm
    @DecimalMin(value = "0.0", message = "Đơn giá không hợp lệ")
    private BigDecimal donGia;

    public Long getSanPhamId() { return sanPhamId; }
    public void setSanPhamId(Long sanPhamId) { this.sanPhamId = sanPhamId; }
    public String getMaLo() { return maLo; }
    public void setMaLo(String maLo) { this.maLo = maLo; }
    public LocalDate getNgaySanXuat() { return ngaySanXuat; }
    public void setNgaySanXuat(LocalDate ngaySanXuat) { this.ngaySanXuat = ngaySanXuat; }
    public LocalDate getHanSuDung() { return hanSuDung; }
    public void setHanSuDung(LocalDate hanSuDung) { this.hanSuDung = hanSuDung; }
    public Long getViTriId() { return viTriId; }
    public void setViTriId(Long viTriId) { this.viTriId = viTriId; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}
