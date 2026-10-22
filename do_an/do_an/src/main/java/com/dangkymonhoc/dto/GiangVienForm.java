package com.dangkymonhoc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GiangVienForm {

    public static final String[] HOC_VI = { "Cử nhân", "Thạc sĩ", "Tiến sĩ", "Phó giáo sư", "Giáo sư" };

    @NotBlank(message = "Vui lòng nhập mã giảng viên.")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Mã giảng viên chỉ gồm chữ in hoa không dấu và số, tối đa 10 ký tự.")
    private String maGV;

    @NotBlank(message = "Vui lòng nhập họ tên.")
    @Size(max = 50, message = "Họ tên tối đa 50 ký tự.")
    private String hoTen;

    @Size(max = 30, message = "Học vị tối đa 30 ký tự.")
    private String hocVi;

    @NotBlank(message = "Vui lòng chọn khoa.")
    private String maKhoa;

    /** Chỉ dùng khi thêm mới: tạo tài khoản GV với tên đăng nhập = mã GV viết thường. */
    private boolean taoTaiKhoan = true;

    private boolean editMode;

    public void setMaGV(String maGV) {
        this.maGV = maGV == null ? null : maGV.trim().toUpperCase();
    }
}
