package com.dangkymonhoc.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.dangkymonhoc.entity.Quyen;
import com.dangkymonhoc.entity.TaiKhoan;

import lombok.Getter;

/**
 * Người dùng đang đăng nhập. Giữ sẵn mã SV/GV và họ tên để hiển thị header
 * và để lọc dữ liệu theo đúng người đăng nhập.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;
    private final boolean enabled;
    private final String maQuyen;
    private final String tenQuyen;
    private final String maSV;
    private final String maGV;
    private final String hoTen;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(TaiKhoan tk) {
        this.username = tk.getTenDangNhap();
        this.password = tk.getMatKhau();
        this.enabled = Boolean.TRUE.equals(tk.getTrangThai());
        this.maQuyen = tk.getQuyen().getMaQuyen();
        this.tenQuyen = tk.getQuyen().getTenQuyen();
        this.maSV = tk.getSinhVien() != null ? tk.getSinhVien().getMaSV() : null;
        this.maGV = tk.getGiangVien() != null ? tk.getGiangVien().getMaGV() : null;
        String chuTaiKhoan = tk.getHoTenChuTaiKhoan();
        this.hoTen = chuTaiKhoan != null ? chuTaiKhoan : tk.getTenDangNhap();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + maQuyen));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public boolean isAdmin() {
        return Quyen.ADMIN.equals(maQuyen);
    }

    /** Chữ cái đầu của tên (từ cuối trong họ tên) để vẽ avatar. */
    public String getInitials() {
        String[] parts = hoTen.trim().split("\\s+");
        String ten = parts[parts.length - 1];
        return ten.isEmpty() ? "?" : ten.substring(0, 1).toUpperCase();
    }
}
