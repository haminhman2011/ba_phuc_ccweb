package com.dangkymonhoc.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.dto.KetQuaLuuDiem;
import com.dangkymonhoc.dto.NhapDiemRow;
import com.dangkymonhoc.dto.PhanCongCard;
import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.PhanCongId;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;

import lombok.RequiredArgsConstructor;

/** Nghiệp vụ của giảng viên: xem danh sách sinh viên (UC08) và nhập điểm (UC09). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiemService {

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhanCongRepository phanCongRepository;
    private final DangKyRepository dangKyRepository;
    private final MonHocRepository monHocRepository;

    /** QĐ7: giảng viên có được phân công dạy môn trong học kỳ không. */
    public boolean duocPhanCong(String maGV, String maMH, String hocKy) {
        return phanCongRepository.existsById(new PhanCongId(maGV, maMH, hocKy));
    }

    public MonHoc layMonHoc(String maMH) {
        return monHocRepository.findById(maMH)
                .orElseThrow(() -> new BusinessException("Không tìm thấy môn học " + maMH + "."));
    }

    /** Sinh viên đã đăng ký lớp của giảng viên (môn, học kỳ). */
    public List<DangKy> danhSachLop(String maGV, String maMH, String hocKy) {
        return dangKyRepository.findDanhSachLop(maGV, maMH, hocKy);
    }

    /** Các dòng của bảng nhập điểm; nếu có {@code nhapTay} thì hiển thị lại giá trị người dùng đã gõ. */
    public List<NhapDiemRow> bangDiem(String maGV, String maMH, String hocKy, Map<String, String> nhapTay,
                                      Set<String> oLoi) {
        List<NhapDiemRow> rows = new ArrayList<>();
        for (DangKy dk : danhSachLop(maGV, maMH, hocKy)) {
            String maSV = dk.getSinhVien().getMaSV();
            String diem = nhapTay != null && nhapTay.containsKey(maSV)
                    ? nhapTay.get(maSV)
                    : dinhDang(dk.getDiem());
            rows.add(new NhapDiemRow(maSV, dk.getSinhVien().getHoTen(), dk.getSinhVien().getLop().getTenLop(),
                    dk.getNgayDangKy(), diem, oLoi != null && oLoi.contains(maSV)));
        }
        return rows;
    }

    /**
     * UC09: kiểm tra toàn bộ điểm trước; chỉ khi tất cả hợp lệ mới lưu (trong một transaction).
     * Ô để trống nghĩa là chưa có điểm.
     */
    @Transactional
    public KetQuaLuuDiem luuDiem(String maGV, String maMH, String hocKy, Map<String, String> nhap) {
        if (!duocPhanCong(maGV, maMH, hocKy)) {
            throw new BusinessException("Bạn không được phân công dạy môn này.");
        }
        List<DangKy> ds = danhSachLop(maGV, maMH, hocKy);
        Set<String> oLoi = new LinkedHashSet<>();
        Map<String, Float> giaTri = new HashMap<>();
        for (DangKy dk : ds) {
            String maSV = dk.getSinhVien().getMaSV();
            if (!nhap.containsKey(maSV)) {
                continue;
            }
            String raw = nhap.get(maSV) == null ? "" : nhap.get(maSV).trim().replace(',', '.');
            if (raw.isEmpty()) {
                giaTri.put(maSV, null);
                continue;
            }
            try {
                float diem = Float.parseFloat(raw);
                // QĐ7: điểm từ 0 đến 10
                if (Float.isNaN(diem) || diem < 0 || diem > 10) {
                    oLoi.add(maSV);
                } else {
                    giaTri.put(maSV, Math.round(diem * 10) / 10f);
                }
            } catch (NumberFormatException e) {
                oLoi.add(maSV);
            }
        }
        if (!oLoi.isEmpty()) {
            return new KetQuaLuuDiem(oLoi, 0);
        }

        int coDiem = 0;
        for (DangKy dk : ds) {
            String maSV = dk.getSinhVien().getMaSV();
            if (giaTri.containsKey(maSV)) {
                dk.setDiem(giaTri.get(maSV));
            }
            if (dk.getDiem() != null) {
                coDiem++;
            }
        }
        return new KetQuaLuuDiem(Set.of(), coDiem);
    }

    /** UC08: thẻ các môn được phân công kèm sĩ số và tiến độ nhập điểm. */
    public List<PhanCongCard> monDuocPhanCong(String maGV, String hocKy) {
        // Chỉ tính sinh viên học lớp của giảng viên này
        Map<String, long[]> thongKe = new HashMap<>();
        for (Object[] row : dangKyRepository.thongKeTheoLop(hocKy)) {
            if (maGV.equals(row[0])) {
                thongKe.put((String) row[1], new long[] { (Long) row[2], (Long) row[3] });
            }
        }
        List<PhanCongCard> cards = new ArrayList<>();
        for (PhanCong pc : phanCongRepository.findByGiangVienAndHocKy(maGV, hocKy)) {
            MonHoc mon = pc.getMonHoc();
            long[] tk = thongKe.getOrDefault(mon.getMaMH(), new long[] { 0, 0 });
            cards.add(new PhanCongCard(mon.getMaMH(), mon.getTenMH(), mon.getSoTinChi(), tk[0], tk[1]));
        }
        return cards;
    }

    public List<String> hocKyCuaGiangVien(String maGV) {
        return phanCongRepository.findHocKyByGiangVien(maGV);
    }

    public long soMonPhanCong(String maGV, String hocKy) {
        return phanCongRepository.countByGiangVienAndHocKy(maGV, hocKy);
    }

    public long soSinhVien(String maGV, String hocKy) {
        return dangKyRepository.countSinhVienCuaGiangVien(maGV, hocKy);
    }

    public long soSinhVienChuaCoDiem(String maGV, String hocKy) {
        return dangKyRepository.countChuaCoDiemCuaGiangVien(maGV, hocKy);
    }

    /** File CSV UTF-8 có BOM để Excel hiển thị đúng tiếng Việt. */
    public byte[] xuatCsv(String maGV, String maMH, String hocKy) {
        StringBuilder sb = new StringBuilder("STT,Mã SV,Họ tên,Lớp,Ngày đăng ký,Điểm\r\n");
        int stt = 1;
        for (DangKy dk : danhSachLop(maGV, maMH, hocKy)) {
            sb.append(stt++).append(',')
              .append(csv(dk.getSinhVien().getMaSV())).append(',')
              .append(csv(dk.getSinhVien().getHoTen())).append(',')
              .append(csv(dk.getSinhVien().getLop().getTenLop())).append(',')
              .append(dk.getNgayDangKy() == null ? "" : dk.getNgayDangKy().format(NGAY)).append(',')
              .append(dinhDang(dk.getDiem()))
              .append("\r\n");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
        out.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private static String dinhDang(Float diem) {
        return diem == null ? "" : String.format(Locale.US, "%.1f", diem);
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
