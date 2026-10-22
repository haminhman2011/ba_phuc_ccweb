package com.dangkymonhoc.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.dto.GiangVienForm;
import com.dangkymonhoc.entity.GiangVien;
import com.dangkymonhoc.entity.Khoa;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.GiangVienRepository;
import com.dangkymonhoc.repository.KhoaRepository;
import com.dangkymonhoc.repository.PhanCongRepository;
import com.dangkymonhoc.repository.TaiKhoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GiangVienService {

    private final GiangVienRepository giangVienRepository;
    private final KhoaRepository khoaRepository;
    private final PhanCongRepository phanCongRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiKhoanService taiKhoanService;
    private final AppProperties appProperties;

    public Page<GiangVien> timKiem(String maKhoa, String q, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), appProperties.getPageSize(), Sort.by("maGV"));
        Page<GiangVien> ds = giangVienRepository.timKiem(TimKiem.loc(maKhoa), TimKiem.like(q), pageable);
        if (ds.isEmpty() && ds.getTotalPages() > 0 && page >= ds.getTotalPages()) {
            return timKiem(maKhoa, q, ds.getTotalPages() - 1);
        }
        return ds;
    }

    public List<GiangVien> tatCa() {
        return giangVienRepository.findAllWithKhoa();
    }

    /** Giảng viên kèm khoa. */
    public GiangVien layChiTiet(String maGV) {
        return giangVienRepository.findChiTiet(maGV)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên " + maGV + "."));
    }

    /** maGV → số môn được phân công trong học kỳ. */
    public Map<String, Long> soMonPhanCong(String hocKy) {
        Map<String, Long> result = new HashMap<>();
        for (Object[] row : phanCongRepository.countTheoGiangVien(hocKy)) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }

    public boolean tonTai(String maGV) {
        return giangVienRepository.existsById(TimKiem.ma(maGV));
    }

    public GiangVienForm layForm(String maGV) {
        GiangVien gv = layChiTiet(maGV);
        GiangVienForm form = new GiangVienForm();
        form.setMaGV(gv.getMaGV());
        form.setHoTen(gv.getHoTen());
        form.setHocVi(gv.getHocVi());
        form.setMaKhoa(gv.getKhoa().getMaKhoa());
        form.setTaoTaiKhoan(false);
        form.setEditMode(true);
        return form;
    }

    @Transactional
    public String luu(GiangVienForm form) {
        String ma = TimKiem.ma(form.getMaGV());
        Khoa khoa = khoaRepository.findById(form.getMaKhoa())
                .orElseThrow(() -> new BusinessException("Khoa đã chọn không tồn tại."));
        GiangVien gv;
        if (form.isEditMode()) {
            gv = giangVienRepository.findById(ma)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên " + ma + "."));
        } else {
            if (giangVienRepository.existsById(ma)) {
                throw new BusinessException("Mã " + ma + " đã tồn tại.");
            }
            gv = new GiangVien();
            gv.setMaGV(ma);
        }
        gv.setHoTen(form.getHoTen().trim());
        gv.setHocVi(form.getHocVi() == null || form.getHocVi().isBlank() ? null : form.getHocVi());
        gv.setKhoa(khoa);
        gv = giangVienRepository.save(gv);

        if (!form.isEditMode() && form.isTaoTaiKhoan()) {
            taiKhoanService.taoChoGiangVien(gv);
        }
        return gv.getHoTen();
    }

    @Transactional
    public String xoa(String maGV) {
        GiangVien gv = giangVienRepository.findById(maGV)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên " + maGV + "."));
        if (phanCongRepository.countByGiangVien(maGV) > 0) {
            throw new BusinessException("Không thể xóa: giảng viên " + gv.getHoTen()
                    + " đang được sử dụng (đã có phân công giảng dạy).");
        }
        taiKhoanRepository.findByGiangVien(maGV).ifPresent(taiKhoanRepository::delete);
        giangVienRepository.delete(gv);
        return gv.getHoTen();
    }

    @Transactional
    public String capTaiKhoan(String maGV) {
        GiangVien gv = giangVienRepository.findById(maGV)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giảng viên " + maGV + "."));
        return taiKhoanService.taoChoGiangVien(gv);
    }
}
