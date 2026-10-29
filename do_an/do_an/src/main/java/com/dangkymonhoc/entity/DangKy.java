package com.dangkymonhoc.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "DANG_KY")
@Getter
@Setter
@NoArgsConstructor
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

    /** Giảng viên dạy lớp sinh viên đã chọn; (MaGV, MaMH, HocKy) là khóa ngoại tới PHAN_CONG. */
    @Column(name = "MaGV", length = 10)
    private String maGV;

    /** Lớp (phân công) sinh viên học, chỉ đọc: ghi qua maGV và khóa chính. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "MaGV", referencedColumnName = "MaGV", insertable = false, updatable = false),
            @JoinColumn(name = "MaMH", referencedColumnName = "MaMH", insertable = false, updatable = false),
            @JoinColumn(name = "HocKy", referencedColumnName = "HocKy", insertable = false, updatable = false)
    }, foreignKey = @ForeignKey(name = "FK_DK_PC"))
    private PhanCong phanCong;

    @Column(name = "NgayDangKy")
    private LocalDate ngayDangKy;

    /** Điểm thang 10; null khi giảng viên chưa nhập (QĐ6, QĐ7). */
    @Column(name = "Diem")
    private Float diem;

    public DangKy(DangKyId id, SinhVien sinhVien, MonHoc monHoc, String maGV, LocalDate ngayDangKy, Float diem) {
        this.id = id;
        this.sinhVien = sinhVien;
        this.monHoc = monHoc;
        this.maGV = maGV;
        this.ngayDangKy = ngayDangKy;
        this.diem = diem;
    }

    public String getHocKy() {
        return id.getHocKy();
    }
}
