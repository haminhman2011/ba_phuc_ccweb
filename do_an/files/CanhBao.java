package com.example.kholanh.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "canh_bao")
public class CanhBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lo_hang_id")
    private LoHang loHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_id")
    private SanPham sanPham;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_canh_bao", nullable = false)
    private LoaiCanhBao loaiCanhBao;

    @Column(name = "noi_dung", nullable = false, length = 255)
    private String noiDung;

    @Column(name = "ngay_canh_bao")
    private LocalDateTime ngayCanhBao = LocalDateTime.now();

    @Column(name = "da_xu_ly", nullable = false)
    private Integer daXuLy = 0;

    public enum LoaiCanhBao { SAP_HET_HAN, QUA_HAN, TON_TOI_THIEU }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LoHang getLoHang() { return loHang; }
    public void setLoHang(LoHang loHang) { this.loHang = loHang; }
    public SanPham getSanPham() { return sanPham; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }
    public LoaiCanhBao getLoaiCanhBao() { return loaiCanhBao; }
    public void setLoaiCanhBao(LoaiCanhBao loaiCanhBao) { this.loaiCanhBao = loaiCanhBao; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public LocalDateTime getNgayCanhBao() { return ngayCanhBao; }
    public void setNgayCanhBao(LocalDateTime ngayCanhBao) { this.ngayCanhBao = ngayCanhBao; }
    public Integer getDaXuLy() { return daXuLy; }
    public void setDaXuLy(Integer daXuLy) { this.daXuLy = daXuLy; }
}
