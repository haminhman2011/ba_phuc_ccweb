package com.dangkymonhoc.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MonHocForm {

    @NotBlank(message = "Vui lòng nhập mã môn.")
    @Pattern(regexp = "^[A-Z0-9]{1,10}$", message = "Mã môn chỉ gồm chữ in hoa không dấu và số, tối đa 10 ký tự.")
    private String maMH;

    @NotBlank(message = "Vui lòng nhập tên môn.")
    @Size(max = 100, message = "Tên môn tối đa 100 ký tự.")
    private String tenMH;

    // QĐ8
    @NotNull(message = "Số tín chỉ phải từ 1 đến 6.")
    @Min(value = 1, message = "Số tín chỉ phải từ 1 đến 6.")
    @Max(value = 6, message = "Số tín chỉ phải từ 1 đến 6.")
    private Integer soTinChi = 3;

    @NotBlank(message = "Vui lòng chọn khoa phụ trách.")
    private String maKhoa;

    private boolean editMode;

    public void setMaMH(String maMH) {
        this.maMH = maMH == null ? null : maMH.trim().toUpperCase();
    }
}
