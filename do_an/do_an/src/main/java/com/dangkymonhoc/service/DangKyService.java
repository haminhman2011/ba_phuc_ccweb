package com.dangkymonhoc.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.dto.KetQuaNhom;
import com.dangkymonhoc.dto.LopHocPhanRow;
import com.dangkymonhoc.dto.MonMoRow;
import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.entity.DangKyId;
import com.dangkymonhoc.entity.LichHoc;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.PhanCongId;
import com.dangkymonhoc.entity.SinhVien;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.LichHocRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;
import com.dangkymonhoc.repository.SinhVienRepository;

import lombok.RequiredArgsConstructor;

/** Nghiệp vụ của sinh viên: đăng ký (UC04, UC05), hủy đăng ký (UC06), xem kết quả (UC07). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DangKyService {

    private final DangKyRepository dangKyRepository;
    private final PhanCongRepository phanCongRepository;
    private final MonHocRepository monHocRepository;
    private final SinhVienRepository sinhVienRepository;
    private final LichHocRepository lichHocRepository;
    private final AppProperties appProperties;
    private final HocKyService hocKyService;

    /**
     * UC04 + UC05: sinh viên chọn lớp (giảng viên) của môn. Kiểm tra lần lượt QĐ3 → QĐ4 → QĐ5 → lịch chính thức
     * → QĐ10 → sức chứa phòng, gặp vi phạm đầu tiên thì dừng.
     *
     * @return bản ghi đăng ký vừa lưu
     */
    @Transactional
    public DangKy dangKy(String maSV, String maMH, String maGV, String hocKy) {
        kiemTraHocKyHienTai(hocKy, "đăng ký");

        // QĐ3: môn phải được mở (có phân công) trong học kỳ
        if (phanCongRepository.countByMonHocAndHocKy(maMH, hocKy) == 0) {
            throw new BusinessException("Môn học này chưa được mở trong học kỳ.");
        }
        MonHoc mon = monHocRepository.findById(maMH)
                .orElseThrow(() -> new BusinessException("Môn học này chưa được mở trong học kỳ."));
        if (maGV == null || !phanCongRepository.existsById(new PhanCongId(maGV, maMH, hocKy))) {
            throw new BusinessException("Vui lòng chọn một lớp của môn " + mon.getTenMH() + ".");
        }

        // QĐ4: không đăng ký trùng môn trong cùng học kỳ
        DangKyId id = new DangKyId(maSV, maMH, hocKy);
        if (dangKyRepository.existsById(id)) {
            throw new BusinessException("Bạn đã đăng ký môn " + mon.getTenMH() + " trong học kỳ này.");
        }

        // QĐ5: tổng tín chỉ sau khi thêm không vượt mức tối đa
        int hienCo = tongTinChi(maSV, hocKy);
        int toiDa = appProperties.getTinChiToiDa();
        if (hienCo + mon.getSoTinChi() > toiDa) {
            throw new BusinessException("Vượt quá " + toiDa + " tín chỉ. Hiện có " + hienCo
                    + " tín chỉ, môn này " + mon.getSoTinChi() + " tín chỉ.");
        }

        // Chỉ đăng ký lớp đã có lịch chính thức
        Optional<LichHoc> lich = lichHocRepository.findByPhanCong(maGV, maMH, hocKy);
        if (lich.isEmpty() || !lich.get().isChinhThuc()) {
            throw new BusinessException(lichHocRepository.countChinhThuc(hocKy) == 0
                    ? "Lịch học kỳ " + hocKy + " chưa được công bố, chưa thể đăng ký."
                    : "Lớp này chưa có lịch học chính thức, chưa thể đăng ký.");
        }
        LichHoc buoi = lich.get();

        // QĐ10: không trùng lịch với các môn đã đăng ký
        for (LichHoc daCo : lichHocRepository.findChinhThucCuaSinhVien(maSV, hocKy)) {
            if (buoi.giaoNhau(daCo)) {
                throw new BusinessException("Trùng lịch với môn " + daCo.getPhanCong().getMonHoc().getTenMH()
                        + " (" + daCo.getThoiGian() + ").");
            }
        }

        // Sức chứa phòng
        int sucChua = buoi.getPhong().getSucChua();
        if (dangKyRepository.countByLop(maGV, maMH, hocKy) >= sucChua) {
            throw new BusinessException("Lớp đã đủ " + sucChua + " sinh viên, vui lòng chọn lớp khác.");
        }

        SinhVien sv = sinhVienRepository.getReferenceById(maSV);
        return dangKyRepository.save(new DangKy(id, sv, mon, maGV, LocalDate.now(), null));
    }

    /**
     * UC06: chỉ hủy được khi môn chưa có điểm (QĐ6).
     *
     * @return tên môn đã hủy
     */
    @Transactional
    public String huy(String maSV, String maMH, String hocKy) {
        kiemTraHocKyHienTai(hocKy, "hủy đăng ký");

        DangKy dk = dangKyRepository.findChiTiet(maSV, maMH, hocKy)
                .orElseThrow(() -> new BusinessException("Bạn chưa đăng ký môn này trong học kỳ."));
        if (dk.getDiem() != null) {
            throw new BusinessException("Môn đã có điểm, không thể hủy.");
        }
        dangKyRepository.delete(dk);
        return dk.getMonHoc().getTenMH();
    }

    /** Chỉ được đăng ký / hủy đăng ký trong học kỳ hiện tại, học kỳ cũ chỉ được xem. */
    private void kiemTraHocKyHienTai(String hocKy, String thaoTac) {
        String hienTai = hocKyService.hienTai();
        if (!hienTai.equals(hocKy)) {
            throw new BusinessException("Chỉ được " + thaoTac + " môn học trong học kỳ hiện tại (" + hienTai + ").");
        }
    }

    public int tongTinChi(String maSV, String hocKy) {
        Long tong = dangKyRepository.tongTinChi(maSV, hocKy);
        return tong == null ? 0 : tong.intValue();
    }

    public List<DangKy> daDangKy(String maSV, String hocKy) {
        return dangKyRepository.findBySinhVienAndHocKy(maSV, hocKy);
    }

    /** Lịch học kỳ đã có buổi chính thức (sinh viên được đăng ký). */
    public boolean lichDaCongBo(String hocKy) {
        return lichHocRepository.countChinhThuc(hocKy) > 0;
    }

    /** maMH → lịch chính thức của lớp sinh viên đang học, ví dụ "Thứ 3, tiết 1–4, P.A101". */
    public Map<String, String> lichDaDangKy(String maSV, String hocKy) {
        Map<String, String> lich = new HashMap<>();
        for (LichHoc l : lichHocRepository.findChinhThucCuaSinhVien(maSV, hocKy)) {
            lich.put(l.getPhanCong().getId().getMaMH(), l.getMoTa());
        }
        return lich;
    }

    /**
     * UC03: các môn được mở trong học kỳ, mỗi môn liệt kê từng lớp (giảng viên + lịch chính thức), đánh dấu
     * môn đã đăng ký, môn sẽ vượt tín chỉ và lớp trùng lịch với môn đã đăng ký (QĐ10).
     */
    public List<MonMoRow> monDuocMo(String hocKy, List<DangKy> daDangKy) {
        Set<String> daDK = new HashSet<>();
        Set<PhanCongId> lopDaDK = new HashSet<>();
        int tong = 0;
        for (DangKy dk : daDangKy) {
            daDK.add(dk.getMonHoc().getMaMH());
            lopDaDK.add(new PhanCongId(dk.getMaGV(), dk.getMonHoc().getMaMH(), hocKy));
            tong += dk.getMonHoc().getSoTinChi();
        }

        // Lịch chính thức của mọi lớp và các buổi sinh viên đang học
        Map<PhanCongId, LichHoc> lich = new HashMap<>();
        List<LichHoc> lichCuaToi = new ArrayList<>();
        for (LichHoc l : lichHocRepository.findByHocKy(hocKy)) {
            if (l.isChinhThuc()) {
                lich.put(l.getPhanCong().getId(), l);
                if (lopDaDK.contains(l.getPhanCong().getId())) {
                    lichCuaToi.add(l);
                }
            }
        }
        Map<PhanCongId, Long> siSo = new HashMap<>();
        for (Object[] row : dangKyRepository.thongKeTheoLop(hocKy)) {
            siSo.put(new PhanCongId((String) row[0], (String) row[1], hocKy), (Long) row[2]);
        }

        // Gom các lớp cùng môn (danh sách đã sắp theo mã môn, mã giảng viên)
        Map<String, List<PhanCong>> theoMon = new LinkedHashMap<>();
        for (PhanCong pc : phanCongRepository.findByHocKy(hocKy)) {
            theoMon.computeIfAbsent(pc.getMonHoc().getMaMH(), k -> new ArrayList<>()).add(pc);
        }

        int toiDa = appProperties.getTinChiToiDa();
        List<MonMoRow> rows = new ArrayList<>();
        for (List<PhanCong> pcs : theoMon.values()) {
            MonHoc mon = pcs.get(0).getMonHoc();
            List<LopHocPhanRow> lops = new ArrayList<>();
            for (PhanCong pc : pcs) {
                LichHoc l = lich.get(pc.getId());
                lops.add(new LopHocPhanRow(pc.getId().getMaGV(), pc.getGiangVien().getHoTen(),
                        l == null ? null : l.getMoTa(), siSo.getOrDefault(pc.getId(), 0L),
                        l == null ? 0 : l.getPhong().getSucChua(), lopDaDK.contains(pc.getId()),
                        l == null ? null : trungLich(l, lichCuaToi)));
            }
            boolean daDangKyMon = daDK.contains(mon.getMaMH());
            boolean vuot = !daDangKyMon && tong + mon.getSoTinChi() > toiDa;
            rows.add(new MonMoRow(mon.getMaMH(), mon.getTenMH(), mon.getSoTinChi(), mon.getKhoa().getTenKhoa(),
                    daDangKyMon, vuot, lops));
        }
        return rows;
    }

    /** QĐ10: thông báo trùng lịch của một lớp với các buổi sinh viên đang học, null nếu không trùng. */
    private static String trungLich(LichHoc lop, List<LichHoc> lichCuaToi) {
        for (LichHoc daCo : lichCuaToi) {
            boolean cungMon = daCo.getPhanCong().getId().getMaMH().equals(lop.getPhanCong().getId().getMaMH());
            if (!cungMon && lop.giaoNhau(daCo)) {
                return "Trùng lịch với môn " + daCo.getPhanCong().getMonHoc().getTenMH()
                        + " (" + daCo.getThoiGian() + ").";
            }
        }
        return null;
    }

    /** Học kỳ sinh viên đã có đăng ký. */
    public List<String> hocKyDaDangKy(String maSV) {
        return dangKyRepository.findHocKyBySinhVien(maSV);
    }

    /** UC07: kết quả học tập; hocKy rỗng = tất cả học kỳ (mới nhất trước). */
    public List<DangKy> ketQua(String maSV, String hocKy) {
        if (hocKy == null || hocKy.isBlank()) {
            List<DangKy> ds = new ArrayList<>(dangKyRepository.findBySinhVien(maSV));
            ds.sort(Comparator.comparing(DangKy::getHocKy, HocKyService.moiNhatTruoc())
                    .thenComparing(dk -> dk.getMonHoc().getMaMH()));
            return ds;
        }
        return dangKyRepository.findBySinhVienAndHocKy(maSV, hocKy);
    }

    public List<KetQuaNhom> nhomTheoHocKy(List<DangKy> dangKys) {
        Map<String, List<DangKy>> nhom = new LinkedHashMap<>();
        for (DangKy dk : dangKys) {
            nhom.computeIfAbsent(dk.getHocKy(), k -> new ArrayList<>()).add(dk);
        }
        return nhom.entrySet().stream().map(e -> new KetQuaNhom(e.getKey(), e.getValue())).toList();
    }

    public long soMonCoDiem(String maSV) {
        return dangKyRepository.countCoDiemBySinhVien(maSV);
    }
}
