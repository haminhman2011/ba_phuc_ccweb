package com.dangkymonhoc.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.dto.MonHocForm;
import com.dangkymonhoc.entity.Khoa;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.KhoaRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonHocService {

    private final MonHocRepository monHocRepository;
    private final KhoaRepository khoaRepository;
    private final DangKyRepository dangKyRepository;
    private final PhanCongRepository phanCongRepository;

    public List<MonHoc> timKiem(String maKhoa, String q) {
        return monHocRepository.timKiem(TimKiem.loc(maKhoa), TimKiem.like(q));
    }

    public List<MonHoc> tatCa() {
        return monHocRepository.findAllSorted();
    }

    /** Mã các môn có phân công (đang mở) trong học kỳ. */
    public Set<String> maMonDuocMo(String hocKy) {
        return new HashSet<>(phanCongRepository.findMaMonDuocMo(hocKy));
    }

    public boolean tonTai(String maMH) {
        return monHocRepository.existsById(TimKiem.ma(maMH));
    }

    @Transactional
    public String luu(MonHocForm form) {
        // QĐ8
        if (form.getSoTinChi() == null || form.getSoTinChi() < 1 || form.getSoTinChi() > 6) {
            throw new BusinessException("Số tín chỉ phải từ 1 đến 6.");
        }
        String ma = TimKiem.ma(form.getMaMH());
        Khoa khoa = khoaRepository.findById(form.getMaKhoa())
                .orElseThrow(() -> new BusinessException("Khoa đã chọn không tồn tại."));
        MonHoc mon;
        if (form.isEditMode()) {
            mon = monHocRepository.findById(ma)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy môn học " + ma + "."));
        } else {
            if (monHocRepository.existsById(ma)) {
                throw new BusinessException("Mã " + ma + " đã tồn tại.");
            }
            mon = new MonHoc();
            mon.setMaMH(ma);
        }
        mon.setTenMH(form.getTenMH().trim());
        mon.setSoTinChi(form.getSoTinChi());
        mon.setKhoa(khoa);
        monHocRepository.save(mon);
        return mon.getTenMH();
    }

    @Transactional
    public String xoa(String maMH) {
        MonHoc mon = monHocRepository.findById(maMH)
                .orElseThrow(() -> new BusinessException("Không tìm thấy môn học " + maMH + "."));
        if (dangKyRepository.countByMonHoc(maMH) > 0 || phanCongRepository.countByMonHoc(maMH) > 0) {
            throw new BusinessException("Không thể xóa: môn " + mon.getTenMH()
                    + " đang được sử dụng (đã có đăng ký hoặc phân công).");
        }
        monHocRepository.delete(mon);
        return mon.getTenMH();
    }
}
