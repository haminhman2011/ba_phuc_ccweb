package com.dangkymonhoc.dto;

import java.util.Set;

/**
 * Kết quả lưu điểm: nếu {@code oLoi} khác rỗng thì không có điểm nào được lưu.
 *
 * @param oLoi    mã SV có điểm không hợp lệ
 * @param soDaLuu số sinh viên có điểm sau khi lưu
 */
public record KetQuaLuuDiem(Set<String> oLoi, int soDaLuu) {

    public boolean hopLe() {
        return oLoi.isEmpty();
    }
}
