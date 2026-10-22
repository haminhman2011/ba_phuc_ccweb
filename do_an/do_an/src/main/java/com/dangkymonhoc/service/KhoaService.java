package com.dangkymonhoc.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.dto.KhoaForm;
import com.dangkymonhoc.dto.KhoaRow;
import com.dangkymonhoc.entity.Khoa;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.GiangVienRepository;
import com.dangkymonhoc.repository.KhoaRepository;
import com.dangkymonhoc.repository.LopRepository;
import com.dangkymonhoc.repository.MonHocRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KhoaService {

    private final KhoaRepository khoaRepository;
    private final LopRepository lopRepository;
    private final GiangVienRepository giangVienRepository;
    private final MonHocRepository monHocRepository;

    public List<KhoaRow> timKiem(String q) {
        return khoaRepository.timKiem(TimKiem.like(q));
    }

    public List<Khoa> tatCa() {
        return khoaRepository.findAllSorted();
    }

    public boolean tonTai(String maKhoa) {
        return khoaRepository.existsById(TimKiem.ma(maKhoa));
    }

    /** Thêm mới hoặc cập nhật tên khoa. Trả về tên khoa để hiển thị thông báo. */
    @Transactional
    public String luu(KhoaForm form) {
        String ma = TimKiem.ma(form.getMaKhoa());
        Khoa khoa;
        if (form.isEditMode()) {
            khoa = khoaRepository.findById(ma)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy khoa " + ma + "."));
        } else {
            if (khoaRepository.existsById(ma)) {
                throw new BusinessException("Mã " + ma + " đã tồn tại.");
            }
            khoa = new Khoa();
            khoa.setMaKhoa(ma);
        }
        khoa.setTenKhoa(form.getTenKhoa().trim());
        khoaRepository.save(khoa);
        return khoa.getTenKhoa();
    }

    @Transactional
    public String xoa(String maKhoa) {
        Khoa khoa = khoaRepository.findById(maKhoa)
                .orElseThrow(() -> new BusinessException("Không tìm thấy khoa " + maKhoa + "."));
        if (lopRepository.countByKhoa(maKhoa) > 0
                || giangVienRepository.countByKhoa(maKhoa) > 0
                || monHocRepository.countByKhoa(maKhoa) > 0) {
            throw new BusinessException("Không thể xóa: khoa " + khoa.getTenKhoa()
                    + " đang được sử dụng (còn lớp, giảng viên hoặc môn học).");
        }
        khoaRepository.delete(khoa);
        return khoa.getTenKhoa();
    }
}
