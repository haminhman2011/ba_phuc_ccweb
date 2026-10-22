package com.dangkymonhoc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaiKhoanForm {

    @NotBlank(message = "Vui lòng nhập tên đăng nhập.")
    @Size(min = 3, max = 30, message = "Tên đăng nhập từ 3 đến 30 ký tự.")
    @Pattern(regexp = "^[a-z0-9_.]*$", message = "Tên đăng nhập chỉ gồm chữ thường không dấu, số, dấu chấm và gạch dưới.")
    private String tenDangNhap;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    @Size(min = 6, max = 100, message = "Mật khẩu tối thiểu 6 ký tự.")
    private String matKhau;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu.")
    private String nhapLaiMatKhau;

    @NotBlank(message = "Vui lòng chọn quyền.")
    private String maQuyen = "SV";

    /** Bắt buộc khi quyền là SV. */
    private String maSV;

    /** Bắt buộc khi quyền là GV. */
    private String maGV;

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap == null ? null : tenDangNhap.trim();
    }
}
