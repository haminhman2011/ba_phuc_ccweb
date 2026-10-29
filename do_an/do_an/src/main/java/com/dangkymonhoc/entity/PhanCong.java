package com.dangkymonhoc.entity;

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

/** Môn có ít nhất một bản ghi phân công trong học kỳ được xem là môn "được mở" (QĐ3). */
@Entity
@Table(name = "PHAN_CONG")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhanCong {

    @EmbeddedId
    private PhanCongId id;

    @MapsId("maGV")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaGV", foreignKey = @ForeignKey(name = "FK_PC_GV"))
    private GiangVien giangVien;

    @MapsId("maMH")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaMH", foreignKey = @ForeignKey(name = "FK_PC_MH"))
    private MonHoc monHoc;

    /** Chỉ để tạo khóa ngoại FK_PC_HK; giá trị học kỳ ghi qua id.hocKy. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HocKy", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_PC_HK"))
    private HocKy thongTinHocKy;

    public PhanCong(PhanCongId id, GiangVien giangVien, MonHoc monHoc) {
        this(id, giangVien, monHoc, null);
    }

    public String getHocKy() {
        return id.getHocKy();
    }
}
