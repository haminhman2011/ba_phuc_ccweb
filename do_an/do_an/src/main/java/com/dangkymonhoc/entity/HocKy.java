package com.dangkymonhoc.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Học kỳ và thời gian học. Ngày bắt đầu là thứ 2 của tuần 1; tuần thứ n bắt đầu từ thứ 2 cách tuần 1
 * (n − 1) tuần. Lịch học chỉ lưu thứ, tiết và khoảng tuần; ngày học cụ thể suy ra từ đây.
 */
@Entity
@Table(name = "HOC_KY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HocKy {

    /** Mẫu HK{1|2|3}-{năm}, ví dụ HK1-2026. */
    @Id
    @Column(name = "MaHK", length = 10)
    private String maHK;

    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "NgayKetThuc", nullable = false)
    private LocalDate ngayKetThuc;

    /** Số tuần học, tuần cuối chưa đủ 7 ngày vẫn tính là một tuần. */
    public int getSoTuan() {
        return (int) (ChronoUnit.DAYS.between(thuHaiTuanDau(), ngayKetThuc) / 7) + 1;
    }

    /** Ngày học của thứ {@code thu} (2–7) trong tuần {@code tuan} (từ 1). */
    public LocalDate ngayHoc(int tuan, int thu) {
        return thuHaiTuanDau().plusWeeks(tuan - 1L).plusDays(thu - 2L);
    }

    /** Tuần chứa {@code ngay}; nhỏ hơn 1 nếu trước học kỳ, lớn hơn số tuần nếu sau học kỳ. */
    public int tuanCua(LocalDate ngay) {
        return (int) Math.floorDiv(ChronoUnit.DAYS.between(thuHaiTuanDau(), ngay), 7) + 1;
    }

    public boolean dangDienRa(LocalDate ngay) {
        return !ngay.isBefore(ngayBatDau) && !ngay.isAfter(ngayKetThuc);
    }

    private LocalDate thuHaiTuanDau() {
        return ngayBatDau.with(DayOfWeek.MONDAY);
    }
}
