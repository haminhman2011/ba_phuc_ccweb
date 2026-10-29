package com.dangkymonhoc.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.dangkymonhoc.common.TietHoc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Một buổi học hiển thị trên lưới tuần và bảng danh sách (MH20, MH21, MH22). */
@Getter
@AllArgsConstructor
public class BuoiHocRow {

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Integer maLich;
    private String maGV;
    private String tenGV;
    private String maMH;
    private String tenMH;
    private int thu;
    private int tietBatDau;
    private int soTiet;
    private int tuanBatDau;
    private int tuanKetThuc;
    /** Ngày học buổi đầu tiên và buổi cuối cùng, null nếu không biết thời gian học kỳ. */
    private LocalDate ngayDau;
    private LocalDate ngayCuoi;
    private String maPhong;
    private int sucChua;
    private long siSo;
    private boolean ghim;
    private boolean chinhThuc;
    /** Lý do xung đột (vi phạm ràng buộc), null nếu hợp lệ. */
    private String xungDot;
    /** Chỉ số màu thẻ 0–7. */
    private int mau;

    /** Cột con trong ngày khi nhiều buổi cùng thời gian (do LichHocService.chiaCot gán). */
    @Setter
    private int lane;
    @Setter
    private int soLane = 1;

    public BuoiHocRow(Integer maLich, String maGV, String tenGV, String maMH, String tenMH, int thu, int tietBatDau,
                      int soTiet, int tuanBatDau, int tuanKetThuc, LocalDate ngayDau, LocalDate ngayCuoi,
                      String maPhong, int sucChua, long siSo, boolean ghim, boolean chinhThuc, String xungDot,
                      int mau) {
        this(maLich, maGV, tenGV, maMH, tenMH, thu, tietBatDau, soTiet, tuanBatDau, tuanKetThuc, ngayDau, ngayCuoi,
                maPhong, sucChua, siSo, ghim, chinhThuc, xungDot, mau, 0, 1);
    }

    public int getTietKetThuc() {
        return tietBatDau + soTiet - 1;
    }

    /** Ví dụ "1–4". */
    public String getTiet() {
        return tietBatDau + "–" + getTietKetThuc();
    }

    /** Ví dụ "07:00–10:30". */
    public String getKhungGio() {
        return TietHoc.khungGio(tietBatDau, getTietKetThuc());
    }

    /** Ví dụ "1–15". */
    public String getTuan() {
        return tuanBatDau + "–" + tuanKetThuc;
    }

    /** Ví dụ "08/09/2026 – 15/12/2026", chuỗi rỗng nếu không biết thời gian học kỳ. */
    public String getKhoangNgay() {
        return ngayDau == null ? "" : ngayDau.format(NGAY) + " – " + ngayCuoi.format(NGAY);
    }

    /** Ví dụ "Thứ 3, tiết 1–4 (07:00–10:30), P.A101". */
    public String getMoTa() {
        return "Thứ " + thu + ", tiết " + getTiet() + " (" + getKhungGio() + "), P." + maPhong;
    }

    /** Ví dụ "Tuần 1–15 (08/09/2026 – 15/12/2026)". */
    public String getThoiGianHoc() {
        return "Tuần " + getTuan() + (ngayDau == null ? "" : " (" + getKhoangNgay() + ")");
    }

    /** Dòng tiêu đề modal sửa buổi. */
    public String getTieuDe() {
        return maMH + " – " + tenMH + " · " + tenGV + " · " + soTiet + " tiết · " + siSo + " SV";
    }
}
