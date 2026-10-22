package com.dangkymonhoc.entity;

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
@Table(name = "LOP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lop {

    @Id
    @Column(name = "MaLop", length = 10)
    private String maLop;

    @Column(name = "TenLop", length = 50, nullable = false)
    private String tenLop;

    @Column(name = "KhoaHoc", length = 10)
    private String khoaHoc;

    // QĐ1: mỗi lớp thuộc đúng một khoa
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKhoa", nullable = false, foreignKey = @ForeignKey(name = "FK_LOP_KHOA"))
    private Khoa khoa;
}
