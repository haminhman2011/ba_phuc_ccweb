package com.manha.lab0910.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "san_pham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Vui lòng nhập mã sản phẩm")
    @Size(max = 20, message = "Mã sản phẩm tối đa 20 ký tự")
    @Column(name = "ma_san_pham", nullable = false, unique = true, length = 20)
    private String maSanPham;

    @NotBlank(message = "Vui lòng nhập tên sản phẩm")
    @Size(max = 150, message = "Tên sản phẩm tối đa 150 ký tự")
    @Column(name = "ten_san_pham", nullable = false, length = 150)
    private String tenSanPham;

    @NotBlank(message = "Vui lòng chọn đơn vị tính")
    @Column(name = "don_vi_tinh", nullable = false, length = 20)
    private String donViTinh;

    @Column(name = "nhiet_do_min")
    private BigDecimal nhietDoMin;

    @Column(name = "nhiet_do_max")
    private BigDecimal nhietDoMax;

    @NotNull(message = "Tồn tối thiểu phải là số nguyên không âm")
    @Min(value = 0, message = "Tồn tối thiểu phải là số nguyên không âm")
    @Column(name = "ton_toi_thieu", nullable = false)
    private Integer tonToiThieu = 0;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai = 1;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public BigDecimal getNhietDoMin() { return nhietDoMin; }
    public void setNhietDoMin(BigDecimal nhietDoMin) { this.nhietDoMin = nhietDoMin; }
    public BigDecimal getNhietDoMax() { return nhietDoMax; }
    public void setNhietDoMax(BigDecimal nhietDoMax) { this.nhietDoMax = nhietDoMax; }
    public Integer getTonToiThieu() { return tonToiThieu; }
    public void setTonToiThieu(Integer tonToiThieu) { this.tonToiThieu = tonToiThieu; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public Integer getTrangThai() { return trangThai; }
    public void setTrangThai(Integer trangThai) { this.trangThai = trangThai; }
}
