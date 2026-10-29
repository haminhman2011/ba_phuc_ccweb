package com.dangkymonhoc.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class HocKyForm {

    @NotBlank(message = "Vui lòng nhập mã học kỳ.")
    @Pattern(regexp = "^HK[123]-\\d{4}$", message = "Mã học kỳ theo mẫu HK{1|2|3}-{năm}, ví dụ HK1-2026.")
    private String maHK;

    @NotNull(message = "Vui lòng chọn ngày bắt đầu.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayBatDau;

    @NotNull(message = "Vui lòng chọn ngày kết thúc.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayKetThuc;

    /** true khi sửa học kỳ đã có (mã không được đổi). */
    private boolean editMode;

    public void setMaHK(String maHK) {
        this.maHK = maHK == null ? null : maHK.trim().toUpperCase();
    }
}
