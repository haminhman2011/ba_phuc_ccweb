package com.dangkymonhoc.dto;

import java.util.List;

/** Kết quả một lần tự động xếp lịch: số buổi xếp được và các phân công không xếp được kèm lý do. */
public record KetQuaXepLich(int soBuoiXep, List<ChuaXepRow> chuaXep) {
}
