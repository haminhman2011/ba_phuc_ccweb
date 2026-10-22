package com.dangkymonhoc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhanCongForm {

    @NotBlank(message = "Thiếu học kỳ.")
    private String hocKy;

    @NotBlank(message = "Vui lòng chọn môn học.")
    private String maMH;

    @NotBlank(message = "Vui lòng chọn giảng viên.")
    private String maGV;
}
