package com.dangkymonhoc.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.dto.LopForm;
import com.dangkymonhoc.dto.LopOption;
import com.dangkymonhoc.dto.LopRow;
import com.dangkymonhoc.entity.Khoa;
import com.dangkymonhoc.entity.Lop;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.KhoaRepository;
import com.dangkymonhoc.repository.LopRepository;
import com.dangkymonhoc.repository.SinhVienRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LopService {

    private final LopRepository lopRepository;
    private final KhoaRepository khoaRepository;
    private final SinhVienRepository sinhVienRepository;

    public List<LopRow> timKiem(String maKhoa, String q) {
        return lopRepository.timKiem(TimKiem.loc(maKhoa), TimKiem.like(q));
    }

    public List<LopOption> theoKhoa(String maKhoa) {
        if (maKhoa == null || maKhoa.isBlank()) {
            return List.of();
        }
        return lopRepository.findOptionsByKhoa(maKhoa.trim());
    }

    public boolean tonTai(String maLop) {
        return lopRepository.existsById(TimKiem.ma(maLop));
    }

    @Transactional
    public String luu(LopForm form) {
        String ma = TimKiem.ma(form.getMaLop());
        Khoa khoa = khoaRepository.findById(form.getMaKhoa())
                .orElseThrow(() -> new BusinessException("Khoa đã chọn không tồn tại."));
        Lop lop;
        if (form.isEditMode()) {
            lop = lopRepository.findById(ma)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy lớp " + ma + "."));
        } else {
            if (lopRepository.existsById(ma)) {
                throw new BusinessException("Mã " + ma + " đã tồn tại.");
            }
            lop = new Lop();
            lop.setMaLop(ma);
        }
        lop.setTenLop(form.getTenLop().trim());
        lop.setKhoaHoc(form.getKhoaHoc() == null || form.getKhoaHoc().isBlank() ? null : form.getKhoaHoc().trim());
        lop.setKhoa(khoa);
        lopRepository.save(lop);
        return lop.getTenLop();
    }

    @Transactional
    public String xoa(String maLop) {
        Lop lop = lopRepository.findById(maLop)
                .orElseThrow(() -> new BusinessException("Không tìm thấy lớp " + maLop + "."));
        if (sinhVienRepository.countByLop(maLop) > 0) {
            throw new BusinessException("Không thể xóa: lớp " + lop.getTenLop() + " đang được sử dụng (còn sinh viên).");
        }
        lopRepository.delete(lop);
        return lop.getTenLop();
    }
}
