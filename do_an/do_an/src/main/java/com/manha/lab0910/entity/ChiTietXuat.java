package com.manha.lab0910.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "chi_tiet_xuat")
public class ChiTietXuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phieu_xuat_id", nullable = false)
    private PhieuXuat phieuXuat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lo_hang_id", nullable = false)
    private LoHang loHang;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia")
    private BigDecimal donGia = BigDecimal.ZERO;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PhieuXuat getPhieuXuat() { return phieuXuat; }
    public void setPhieuXuat(PhieuXuat phieuXuat) { this.phieuXuat = phieuXuat; }
    public LoHang getLoHang() { return loHang; }
    public void setLoHang(LoHang loHang) { this.loHang = loHang; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}
