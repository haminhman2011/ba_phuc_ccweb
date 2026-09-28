package com.manha.lab0910.service;

import com.manha.lab0910.entity.NguoiDung;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Lớp bọc entity NguoiDung lại cho Spring Security dùng.
 * Spring Security không biết gì về entity của mình, nó chỉ hiểu UserDetails.
 */
public class NguoiDungChiTiet implements UserDetails {

    private final NguoiDung nguoiDung;

    public NguoiDungChiTiet(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring Security quy ước quyền phải bắt đầu bằng "ROLE_" thì hasRole() mới nhận.
        // vai_tro = THU_KHO  ->  ROLE_THU_KHO

        // ADMIN là tài khoản toàn quyền: cấp luôn cả hai quyền nghiệp vụ,
        // nhờ vậy mọi rule trong SecurityConfig và mọi sec:authorize trong
        // template đều tự động cho qua, không phải sửa thêm chỗ nào.
        if (nguoiDung.getVaiTro() == NguoiDung.VaiTro.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_THU_KHO"),
                    new SimpleGrantedAuthority("ROLE_QUAN_LY"));
        }

        return List.of(new SimpleGrantedAuthority("ROLE_" + nguoiDung.getVaiTro().name()));
    }

    @Override
    public String getPassword() {
        return nguoiDung.getMatKhau();   // chuỗi BCrypt lưu trong DB
    }

    @Override
    public String getUsername() {
        return nguoiDung.getTenDangNhap();
    }

    // (UC1 - luồng phụ 3c) trang_thai = 0 nghĩa là tài khoản bị khoá.
    // Trả về false thì Spring Security tự ném DisabledException.
    @Override
    public boolean isEnabled() {
        return nguoiDung.getTrangThai() != null && nguoiDung.getTrangThai() == 1;
    }

    // Mấy hàm dưới để template lấy ra hiển thị trên sidebar / navbar
    public Long getId() {
        return nguoiDung.getId();
    }

    public String getHoTen() {
        return nguoiDung.getHoTen();
    }

    public NguoiDung.VaiTro getVaiTro() {
        return nguoiDung.getVaiTro();
    }

    // Tên vai trò hiển thị cho người dùng đọc, thay vì THU_KHO / QUAN_LY / ADMIN
    public String getTenVaiTro() {
        return switch (nguoiDung.getVaiTro()) {
            case THU_KHO -> "Thủ kho";
            case QUAN_LY -> "Quản lý";
            case ADMIN   -> "Quản trị viên";
        };
    }
}
