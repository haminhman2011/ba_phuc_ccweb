package com.dangkymonhoc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoiMatKhauForm {

    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại.")
    private String matKhauHienTai;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới.")
    @Size(min = 6, max = 100, message = "Mật khẩu mới tối thiểu 6 ký tự.")
    private String matKhauMoi;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu mới.")
    private String nhapLaiMatKhau;
}
