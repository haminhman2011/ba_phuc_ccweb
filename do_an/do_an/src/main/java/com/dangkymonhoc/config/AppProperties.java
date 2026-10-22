package com.dangkymonhoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/** Các tham số nghiệp vụ đọc từ application.properties (tiền tố app.*). */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** Học kỳ đang mở đăng ký, ví dụ HK1-2026. */
    private String hocKyHienTai = "HK1-2026";

    /** QĐ5: tổng tín chỉ tối đa một sinh viên được đăng ký trong một học kỳ. */
    private int tinChiToiDa = 24;

    /** Số dòng mỗi trang ở các danh sách có phân trang. */
    private int pageSize = 10;
}
