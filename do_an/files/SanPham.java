package com.example.kholanh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "san_pham")
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_san_pham", nullable = false, unique = true, length = 20)
    private String maSanPham;

    @Column(name = "ten_san_pham", nullable = false, length = 150)
    private String tenSanPham;

    @Column(name = "don_vi_tinh", nullable = false, length = 20)
    private String donViTinh;

    @Column(name = "nhiet_do_min")
    private BigDecimal nhietDoMin;

    @Column(name = "nhiet_do_max")
    private BigDecimal nhietDoMax;

    @Column(name = "ton_toi_thieu", nullable = false)
    private Integer tonToiThieu = 0;

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
