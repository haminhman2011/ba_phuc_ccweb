package com.dangkymonhoc.common;

/**
 * Quyền của tài khoản (QĐ9). Danh sách cố định nên gán cứng ở đây thay vì để trong bảng;
 * cột TAI_KHOAN.MaQuyen lưu tên hằng (ADMIN, GV, SV).
 */
public enum Quyen {

    ADMIN("Quản trị viên"),
    GV("Giảng viên"),
    SV("Sinh viên");

    private final String tenQuyen;

    Quyen(String tenQuyen) {
        this.tenQuyen = tenQuyen;
    }

    /** Mã quyền, cũng là hậu tố ROLE_ của Spring Security. */
    public String getMaQuyen() {
        return name();
    }

    public String getTenQuyen() {
        return tenQuyen;
    }

    /** Quyền theo mã, null nếu mã rỗng hoặc không hợp lệ. */
    public static Quyen tim(String maQuyen) {
        if (maQuyen == null) {
            return null;
        }
        for (Quyen q : values()) {
            if (q.name().equals(maQuyen.trim())) {
                return q;
            }
        }
        return null;
    }
}
