package com.dangkymonhoc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Một buổi học trong tuần của một phân công. Mỗi phân công học 1 buổi/tuần,
 * số tiết bằng số tín chỉ của môn; sáng tiết 1–5, chiều tiết 6–10; học từ tuần TuanBatDau đến TuanKetThuc.
 */
@Entity
@Table(name = "LICH_HOC", uniqueConstraints = @UniqueConstraint(name = "UQ_LH_PHAN_CONG",
        columnNames = { "MaGV", "MaMH", "HocKy" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichHoc {

    public static final String NHAP = "NHAP";
    public static final String CHINH_THUC = "CHINH_THUC";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLich")
    private Integer maLich;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns(value = {
            @JoinColumn(name = "MaGV", referencedColumnName = "MaGV", nullable = false),
            @JoinColumn(name = "MaMH", referencedColumnName = "MaMH", nullable = false),
            @JoinColumn(name = "HocKy", referencedColumnName = "HocKy", nullable = false)
    }, foreignKey = @ForeignKey(name = "FK_LH_PC"))
    private PhanCong phanCong;

    /** Thứ trong tuần, 2 đến 7. */
    @Column(name = "Thu", nullable = false)
    private Integer thu;

    @Column(name = "TietBatDau", nullable = false)
    private Integer tietBatDau;

    @Column(name = "SoTiet", nullable = false)
    private Integer soTiet;

    /** Học từ tuần TuanBatDau đến TuanKetThuc của học kỳ (tuần 1 bắt đầu từ ngày bắt đầu học kỳ). */
    @Column(name = "TuanBatDau", nullable = false)
    private Integer tuanBatDau;

    @Column(name = "TuanKetThuc", nullable = false)
    private Integer tuanKetThuc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MaPhong", nullable = false, foreignKey = @ForeignKey(name = "FK_LH_PHONG"))
    private Phong phong;

    /** NHAP hoặc CHINH_THUC. */
    @Column(name = "TrangThai", length = 15, nullable = false)
    private String trangThai;

    /** Buổi đã ghim thì tự động xếp lại không di chuyển. */
    @Column(name = "Ghim", nullable = false)
    private Boolean ghim;

    /** Buổi nháp chưa lưu, dùng khi thử một vị trí. */
    public LichHoc(PhanCong phanCong, int thu, int tietBatDau, int soTiet, int tuanBatDau, int tuanKetThuc,
                   Phong phong) {
        this(null, phanCong, thu, tietBatDau, soTiet, tuanBatDau, tuanKetThuc, phong, NHAP, false);
    }

    public int getTietKetThuc() {
        return tietBatDau + soTiet - 1;
    }

    public boolean hocTrongTuan(int tuan) {
        return tuanBatDau <= tuan && tuan <= tuanKetThuc;
    }

    public boolean isChinhThuc() {
        return CHINH_THUC.equals(trangThai);
    }

    public boolean isDaGhim() {
        return Boolean.TRUE.equals(ghim);
    }

    /** Hai buổi giao nhau khi cùng thứ, khoảng tiết chồng lên nhau và có tuần học chung. */
    public boolean giaoNhau(LichHoc khac) {
        return thu.equals(khac.thu) && tietBatDau <= khac.getTietKetThuc() && khac.tietBatDau <= getTietKetThuc()
                && tuanBatDau <= khac.tuanKetThuc && khac.tuanBatDau <= tuanKetThuc;
    }

    /** Ví dụ "Thứ 3, tiết 1–4, tuần 1–15". */
    public String getThoiGian() {
        return "Thứ " + thu + ", tiết " + tietBatDau + "–" + getTietKetThuc() + ", tuần " + tuanBatDau + "–"
                + tuanKetThuc;
    }

    /** Ví dụ "Thứ 3, tiết 1–4, P.A101". */
    public String getMoTa() {
        return getThoiGian() + ", P." + phong.getMaPhong();
    }
}
