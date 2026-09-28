package com.manha.lab0910.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "phieu_xuat")
public class PhieuXuat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 30)
    private String maPhieu;

    @Column(name = "ngay_xuat")
    private LocalDateTime ngayXuat = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xuat_id", nullable = false)
    private NguoiDung nguoiXuat;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_xuat", nullable = false)
    private LoaiXuat loaiXuat = LoaiXuat.XUAT_BAN;

    @Column(name = "ly_do", length = 255)
    private String lyDo;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @OneToMany(mappedBy = "phieuXuat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietXuat> chiTiet = new ArrayList<>();

    public enum LoaiXuat { XUAT_BAN, XUAT_HUY }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }
    public LocalDateTime getNgayXuat() { return ngayXuat; }
    public void setNgayXuat(LocalDateTime ngayXuat) { this.ngayXuat = ngayXuat; }
    public NguoiDung getNguoiXuat() { return nguoiXuat; }
    public void setNguoiXuat(NguoiDung nguoiXuat) { this.nguoiXuat = nguoiXuat; }
    public LoaiXuat getLoaiXuat() { return loaiXuat; }
    public void setLoaiXuat(LoaiXuat loaiXuat) { this.loaiXuat = loaiXuat; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public List<ChiTietXuat> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<ChiTietXuat> chiTiet) { this.chiTiet = chiTiet; }
}
