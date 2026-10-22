package com.dangkymonhoc.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.dto.KetQuaNhom;
import com.dangkymonhoc.dto.MonMoRow;
import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.entity.DangKyId;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.SinhVien;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.DangKyRepository;
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
    private final AppProperties appProperties;

    /**
     * UC04 + UC05: kiểm tra lần lượt QĐ3 → QĐ4 → QĐ5, gặp vi phạm đầu tiên thì dừng.
     *
     * @return bản ghi đăng ký vừa lưu
     */
    @Transactional
    public DangKy dangKy(String maSV, String maMH, String hocKy) {
        // QĐ3: môn phải được mở (có phân công) trong học kỳ
        if (phanCongRepository.countByMonHocAndHocKy(maMH, hocKy) == 0) {
            throw new BusinessException("Môn học này chưa được mở trong học kỳ.");
        }
        MonHoc mon = monHocRepository.findById(maMH)
                .orElseThrow(() -> new BusinessException("Môn học này chưa được mở trong học kỳ."));

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

        SinhVien sv = sinhVienRepository.getReferenceById(maSV);
        return dangKyRepository.save(new DangKy(id, sv, mon, LocalDate.now(), null));
    }

    /**
     * UC06: chỉ hủy được khi môn chưa có điểm (QĐ6).
     *
     * @return tên môn đã hủy
     */
    @Transactional
    public String huy(String maSV, String maMH, String hocKy) {
        DangKy dk = dangKyRepository.findChiTiet(maSV, maMH, hocKy)
                .orElseThrow(() -> new BusinessException("Bạn chưa đăng ký môn này trong học kỳ."));
        if (dk.getDiem() != null) {
            throw new BusinessException("Môn đã có điểm, không thể hủy.");
        }
        dangKyRepository.delete(dk);
        return dk.getMonHoc().getTenMH();
    }

    public int tongTinChi(String maSV, String hocKy) {
        Long tong = dangKyRepository.tongTinChi(maSV, hocKy);
        return tong == null ? 0 : tong.intValue();
    }

    public List<DangKy> daDangKy(String maSV, String hocKy) {
        return dangKyRepository.findBySinhVienAndHocKy(maSV, hocKy);
    }

    /** UC03: các môn được mở trong học kỳ, đánh dấu môn đã đăng ký và môn sẽ vượt tín chỉ. */
    public List<MonMoRow> monDuocMo(String hocKy, List<DangKy> daDangKy) {
        Set<String> daDK = new HashSet<>();
        int tong = 0;
        for (DangKy dk : daDangKy) {
            daDK.add(dk.getMonHoc().getMaMH());
            tong += dk.getMonHoc().getSoTinChi();
        }

        // Gom các giảng viên cùng dạy một môn (danh sách đã sắp theo mã môn)
        Map<String, List<PhanCong>> theoMon = new LinkedHashMap<>();
        for (PhanCong pc : phanCongRepository.findByHocKy(hocKy)) {
            theoMon.computeIfAbsent(pc.getMonHoc().getMaMH(), k -> new ArrayList<>()).add(pc);
        }

        int toiDa = appProperties.getTinChiToiDa();
        List<MonMoRow> rows = new ArrayList<>();
        for (List<PhanCong> pcs : theoMon.values()) {
            MonHoc mon = pcs.get(0).getMonHoc();
            String giangVien = String.join(", ", pcs.stream().map(pc -> pc.getGiangVien().getHoTen()).toList());
            boolean daDangKyMon = daDK.contains(mon.getMaMH());
            boolean vuot = !daDangKyMon && tong + mon.getSoTinChi() > toiDa;
            rows.add(new MonMoRow(mon.getMaMH(), mon.getTenMH(), mon.getSoTinChi(), mon.getKhoa().getTenKhoa(),
                    giangVien, daDangKyMon, vuot));
        }
        return rows;
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
