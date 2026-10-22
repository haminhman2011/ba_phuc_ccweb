package com.dangkymonhoc.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SINH_VIEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SinhVien {

    @Id
    @Column(name = "MaSV", length = 10)
    private String maSV;

    @Column(name = "HoTen", length = 50, nullable = false)
    private String hoTen;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    /** "Nam" hoặc "Nữ". */
    @Column(name = "GioiTinh", length = 5)
    private String gioiTinh;

    // QĐ1: mỗi sinh viên thuộc đúng một lớp
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaLop", nullable = false, foreignKey = @ForeignKey(name = "FK_SV_LOP"))
    private Lop lop;
}
