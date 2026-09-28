package com.example.kholanh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "phieu_nhap")
public class PhieuNhap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 30)
    private String maPhieu;

    @Column(name = "ngay_nhap")
    private LocalDateTime ngayNhap = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_nhap_id", nullable = false)
    private NguoiDung nguoiNhap;

    @Column(name = "nha_cung_cap", length = 150)
    private String nhaCungCap;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    // 1 phiếu nhập có nhiều chi tiết
    @OneToMany(mappedBy = "phieuNhap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietNhap> chiTiet = new ArrayList<>();

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }
    public LocalDateTime getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(LocalDateTime ngayNhap) { this.ngayNhap = ngayNhap; }
    public NguoiDung getNguoiNhap() { return nguoiNhap; }
    public void setNguoiNhap(NguoiDung nguoiNhap) { this.nguoiNhap = nguoiNhap; }
    public String getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(String nhaCungCap) { this.nhaCungCap = nhaCungCap; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public List<ChiTietNhap> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<ChiTietNhap> chiTiet) { this.chiTiet = chiTiet; }
}
