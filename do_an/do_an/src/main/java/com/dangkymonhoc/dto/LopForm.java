package com.dangkymonhoc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LopForm {

    @NotBlank(message = "Vui lòng nhập mã lớp.")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Mã lớp chỉ gồm chữ in hoa không dấu và số, tối đa 10 ký tự.")
    private String maLop;

    @NotBlank(message = "Vui lòng nhập tên lớp.")
    @Size(max = 50, message = "Tên lớp tối đa 50 ký tự.")
    private String tenLop;

    @Size(max = 10, message = "Khóa học tối đa 10 ký tự.")
    private String khoaHoc;

    @NotBlank(message = "Vui lòng chọn khoa.")
    private String maKhoa;

    private boolean editMode;

    public void setMaLop(String maLop) {
        this.maLop = maLop == null ? null : maLop.trim().toUpperCase();
    }
}
