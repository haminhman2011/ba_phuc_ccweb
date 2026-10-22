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
@Table(name = "MON_HOC")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonHoc {

    @Id
    @Column(name = "MaMH", length = 10)
    private String maMH;

    @Column(name = "TenMH", length = 100, nullable = false)
    private String tenMH;

    /** QĐ8: từ 1 đến 6. */
    @Column(name = "SoTinChi", nullable = false)
    private Integer soTinChi;

    // QĐ2: mỗi môn học do đúng một khoa phụ trách
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaKhoa", nullable = false, foreignKey = @ForeignKey(name = "FK_MH_KHOA"))
    private Khoa khoa;
}
