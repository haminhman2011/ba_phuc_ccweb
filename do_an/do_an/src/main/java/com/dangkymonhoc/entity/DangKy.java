package com.dangkymonhoc.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DANG_KY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DangKy {

    @EmbeddedId
    private DangKyId id;

    @MapsId("maSV")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSV", foreignKey = @ForeignKey(name = "FK_DK_SV"))
    private SinhVien sinhVien;

    @MapsId("maMH")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaMH", foreignKey = @ForeignKey(name = "FK_DK_MH"))
    private MonHoc monHoc;

    @Column(name = "NgayDangKy")
    private LocalDate ngayDangKy;

    /** Điểm thang 10; null khi giảng viên chưa nhập (QĐ6, QĐ7). */
    @Column(name = "Diem")
    private Float diem;

    public String getHocKy() {
        return id.getHocKy();
    }
}
