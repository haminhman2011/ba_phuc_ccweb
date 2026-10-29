package com.dangkymonhoc.dto;

import com.dangkymonhoc.entity.HocKy;

/**
 * Một dòng ở màn hình quản lý học kỳ.
 *
 * @param trangThai DA_QUA, HIEN_TAI hoặc SAP_TOI (xem HocKyService)
 */
public record HocKyRow(HocKy hocKy, String trangThai, long soPhanCong) {
}
