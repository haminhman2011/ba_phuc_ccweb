package com.dangkymonhoc.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SinhVienForm {

    @NotBlank(message = "Vui lòng nhập mã sinh viên.")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Mã sinh viên chỉ gồm chữ in hoa không dấu và số, tối đa 10 ký tự.")
    private String maSV;

    @NotBlank(message = "Vui lòng nhập họ tên.")
    @Size(max = 50, message = "Họ tên tối đa 50 ký tự.")
    private String hoTen;

    @Past(message = "Ngày sinh phải trước ngày hiện tại.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngaySinh;

    @Pattern(regexp = "^(Nam|Nữ)?$", message = "Giới tính chỉ nhận Nam hoặc Nữ.")
    private String gioiTinh = "Nam";

    @NotBlank(message = "Vui lòng chọn khoa.")
    private String maKhoa;

    @NotBlank(message = "Vui lòng chọn lớp.")
    private String maLop;

    /** Chỉ dùng khi thêm mới: tạo tài khoản SV với tên đăng nhập = mã SV viết thường. */
    private boolean taoTaiKhoan = true;

    private boolean editMode;

    public void setMaSV(String maSV) {
        this.maSV = maSV == null ? null : maSV.trim().toUpperCase();
    }
}
