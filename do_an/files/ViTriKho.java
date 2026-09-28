package com.example.kholanh.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "vi_tri_kho")
public class ViTriKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_vi_tri", nullable = false, unique = true, length = 20)
    private String maViTri;

    @Column(name = "ten_vi_tri", nullable = false, length = 100)
    private String tenViTri;

    @Column(name = "nhiet_do_hien_tai")
    private BigDecimal nhietDoHienTai;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaViTri() { return maViTri; }
    public void setMaViTri(String maViTri) { this.maViTri = maViTri; }
    public String getTenViTri() { return tenViTri; }
    public void setTenViTri(String tenViTri) { this.tenViTri = tenViTri; }
    public BigDecimal getNhietDoHienTai() { return nhietDoHienTai; }
    public void setNhietDoHienTai(BigDecimal nhietDoHienTai) { this.nhietDoHienTai = nhietDoHienTai; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}
