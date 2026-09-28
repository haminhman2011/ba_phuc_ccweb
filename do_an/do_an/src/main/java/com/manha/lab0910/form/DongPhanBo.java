package com.manha.lab0910.form;

import java.time.LocalDate;

/**
 * (UC4) Một dòng của bảng phân bổ FEFO: trừ bao nhiêu ở lô nào.
 * Đây là kết quả hệ thống tự tính, người dùng chỉ xem rồi xác nhận.
 */
public class DongPhanBo {

    private Long loHangId;
    private String maLo;
    private LocalDate hanSuDung;
    private int tonHienCo;
    private int soLuongTru;
    private boolean quaHan;

    public DongPhanBo(Long loHangId, String maLo, LocalDate hanSuDung,
                      int tonHienCo, int soLuongTru, boolean quaHan) {
        this.loHangId = loHangId;
        this.maLo = maLo;
        this.hanSuDung = hanSuDung;
        this.tonHienCo = tonHienCo;
        this.soLuongTru = soLuongTru;
        this.quaHan = quaHan;
    }

    public Long getLoHangId() { return loHangId; }
    public String getMaLo() { return maLo; }
    public LocalDate getHanSuDung() { return hanSuDung; }
    public int getTonHienCo() { return tonHienCo; }
    public int getSoLuongTru() { return soLuongTru; }
    public boolean isQuaHan() { return quaHan; }

    // Lô bị trừ hết sạch sau lần xuất này
    public boolean isTruHet() {
        return soLuongTru == tonHienCo;
    }
}
