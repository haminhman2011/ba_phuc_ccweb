package com.dangkymonhoc.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.dangkymonhoc.common.Quyen;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TAI_KHOAN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaiKhoan {

    @Id
    @Column(name = "TenDangNhap", length = 30)
    private String tenDangNhap;

    /** Mật khẩu đã băm BCrypt. */
    @Column(name = "MatKhau", length = 255, nullable = false)
    private String matKhau;

    /** true: hoạt động, false: bị khóa (QĐ9). */
    @Column(name = "TrangThai", nullable = false)
    private Boolean trangThai = true;

    /** Lưu dạng chuỗi ADMIN / GV / SV (VARCHAR, không dùng kiểu ENUM của MySQL). */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "MaQuyen", length = 10, nullable = false)
    private Quyen quyen;

    /** Chỉ có ở tài khoản sinh viên. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSV", unique = true, foreignKey = @ForeignKey(name = "FK_TK_SV"))
    private SinhVien sinhVien;

    /** Chỉ có ở tài khoản giảng viên. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaGV", unique = true, foreignKey = @ForeignKey(name = "FK_TK_GV"))
    private GiangVien giangVien;

    /** Họ tên chủ tài khoản (SV/GV), null với tài khoản quản trị. */
    public String getHoTenChuTaiKhoan() {
        if (sinhVien != null) {
            return sinhVien.getHoTen();
        }
        if (giangVien != null) {
            return giangVien.getHoTen();
        }
        return null;
    }
}
