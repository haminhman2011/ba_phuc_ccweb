package com.dangkymonhoc.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.dto.PhanCongForm;
import com.dangkymonhoc.entity.GiangVien;
import com.dangkymonhoc.entity.LichHoc;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.PhanCongId;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.GiangVienRepository;
import com.dangkymonhoc.repository.LichHocRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhanCongService {

    private final PhanCongRepository phanCongRepository;
    private final DangKyRepository dangKyRepository;
    private final MonHocRepository monHocRepository;
    private final GiangVienRepository giangVienRepository;
    private final LichHocRepository lichHocRepository;
    private final HocKyService hocKyService;

    public List<PhanCong> theoHocKy(String hocKy) {
        return phanCongRepository.findByHocKy(hocKy);
    }

    /** "maGV:maMH" → số sinh viên đã đăng ký lớp trong học kỳ. */
    public Map<String, Long> soSinhVienTheoLop(String hocKy) {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : dangKyRepository.thongKeTheoLop(hocKy)) {
            result.put(row[0] + ":" + row[1], (Long) row[2]);
        }
        return result;
    }

    /** "maGV:maMH" → buổi học của phân công (nháp hoặc chính thức). */
    public Map<String, LichHoc> lichTheoPhanCong(String hocKy) {
        Map<String, LichHoc> result = new HashMap<>();
        for (LichHoc l : lichHocRepository.findByHocKy(hocKy)) {
            result.put(l.getPhanCong().getId().getMaGV() + ":" + l.getPhanCong().getId().getMaMH(), l);
        }
        return result;
    }

    /** UC15: mở môn trong học kỳ bằng cách phân công giảng viên. */
    @Transactional
    public String them(PhanCongForm form) {
        String hocKy = form.getHocKy().trim().toUpperCase();
        if (!hocKyService.tonTai(hocKy)) {
            throw new BusinessException("Học kỳ " + hocKy + " chưa được khai báo. Hãy thêm học kỳ ở mục Học kỳ trước.");
        }
        MonHoc mon = monHocRepository.findById(form.getMaMH())
                .orElseThrow(() -> new BusinessException("Môn học đã chọn không tồn tại."));
        GiangVien gv = giangVienRepository.findById(form.getMaGV())
                .orElseThrow(() -> new BusinessException("Giảng viên đã chọn không tồn tại."));
        PhanCongId id = new PhanCongId(gv.getMaGV(), mon.getMaMH(), hocKy);
        if (phanCongRepository.existsById(id)) {
            throw new BusinessException("Giảng viên này đã được phân công môn này trong học kỳ.");
        }
        phanCongRepository.save(new PhanCong(id, gv, mon));
        return "Đã phân công " + gv.getHoTen() + " dạy môn " + mon.getTenMH() + " trong học kỳ " + hocKy + ".";
    }

    /**
     * Không cho xóa phân công đã có lịch chính thức (sinh viên đã thấy lịch) hoặc lớp đã có sinh viên đăng ký.
     * Buổi nháp của phân công bị xóa theo.
     */
    @Transactional
    public void xoa(String maGV, String maMH, String hocKy) {
        PhanCongId id = new PhanCongId(maGV, maMH, hocKy);
        PhanCong pc = phanCongRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phân công cần xóa."));
        if (lichHocRepository.countChinhThucCuaPhanCong(maGV, maMH, hocKy) > 0) {
            throw new BusinessException("Không thể xóa: phân công đã có lịch học chính thức.");
        }
        if (dangKyRepository.countByLop(maGV, maMH, hocKy) > 0) {
            throw new BusinessException("Không thể xóa: lớp của giảng viên này đã có sinh viên đăng ký.");
        }
        lichHocRepository.findByPhanCong(maGV, maMH, hocKy).ifPresent(lichHocRepository::delete);
        phanCongRepository.delete(pc);
    }
}
