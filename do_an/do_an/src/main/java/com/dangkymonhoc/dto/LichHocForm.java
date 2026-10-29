package com.dangkymonhoc.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Modal sửa một buổi học hoặc xếp tay một phân công (MH20). */
@Data
public class LichHocForm {

    /** null khi xếp tay phân công chưa có lịch. */
    private Integer maLich;

    @NotBlank(message = "Thiếu giảng viên.")
    private String maGV;

    @NotBlank(message = "Thiếu môn học.")
    private String maMH;

    @NotBlank(message = "Thiếu học kỳ.")
    private String hocKy;

    @NotNull(message = "Vui lòng chọn thứ.")
    @Min(value = 2, message = "Thứ phải từ 2 đến 7.")
    @Max(value = 7, message = "Thứ phải từ 2 đến 7.")
    private Integer thu;

    @NotNull(message = "Vui lòng chọn tiết bắt đầu.")
    private Integer tietBatDau;

    /** Học từ tuần này, đủ số tuần của môn; null thì từ tuần 1. */
    @Min(value = 1, message = "Tuần bắt đầu phải từ 1.")
    private Integer tuanBatDau;

    @NotBlank(message = "Vui lòng chọn phòng học.")
    private String maPhong;

    private boolean ghim;

    /** Đã xác nhận sửa buổi thuộc lịch chính thức (sinh viên đã thấy lịch). */
    private boolean xacNhan;

    // Ba trường dưới chỉ để hiển thị lại modal khi server báo lỗi
    private int soTiet;
    private String tieuDe;
    private boolean chinhThuc;
}
