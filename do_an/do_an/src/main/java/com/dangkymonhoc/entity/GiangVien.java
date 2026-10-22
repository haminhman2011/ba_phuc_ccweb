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
@Table(name = "GIANG_VIEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiangVien {

    @Id
    @Column(name = "MaGV", length = 10)
    private String maGV;

    @Column(name = "HoTen", length = 50, nullable = false)
    private String hoTen;

    @Column(name = "HocVi", length = 30)
    private String hocVi;

    // QĐ2: mỗi giảng viên thuộc đúng một khoa
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKhoa", nullable = false, foreignKey = @ForeignKey(name = "FK_GV_KHOA"))
    private Khoa khoa;
}
