package com.dangkymonhoc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KhoaForm {

    @NotBlank(message = "Vui lòng nhập mã khoa.")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Mã khoa chỉ gồm chữ in hoa không dấu và số, tối đa 10 ký tự.")
    private String maKhoa;

    @NotBlank(message = "Vui lòng nhập tên khoa.")
    @Size(max = 100, message = "Tên khoa tối đa 100 ký tự.")
    private String tenKhoa;

    /** true khi sửa bản ghi đã có (mã khoa không được đổi). */
    private boolean editMode;

    public void setMaKhoa(String maKhoa) {
        this.maKhoa = maKhoa == null ? null : maKhoa.trim().toUpperCase();
    }
}
