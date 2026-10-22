package com.dangkymonhoc.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.dto.SinhVienForm;
import com.dangkymonhoc.entity.Lop;
import com.dangkymonhoc.entity.SinhVien;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.LopRepository;
import com.dangkymonhoc.repository.SinhVienRepository;
import com.dangkymonhoc.repository.TaiKhoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SinhVienService {

    private final SinhVienRepository sinhVienRepository;
    private final LopRepository lopRepository;
    private final DangKyRepository dangKyRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final TaiKhoanService taiKhoanService;
    private final AppProperties appProperties;

    public Page<SinhVien> timKiem(String maKhoa, String maLop, String q, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0), appProperties.getPageSize(), Sort.by("maSV"));
        Page<SinhVien> ds = sinhVienRepository.timKiem(TimKiem.loc(maKhoa), TimKiem.loc(maLop), TimKiem.like(q), pageable);
        // Trang vượt quá số trang (ví dụ vừa xóa dòng cuối của trang cuối): lùi về trang cuối
        if (ds.isEmpty() && ds.getTotalPages() > 0 && page >= ds.getTotalPages()) {
            return timKiem(maKhoa, maLop, q, ds.getTotalPages() - 1);
        }
        return ds;
    }

    /** Sinh viên kèm lớp và khoa. */
    public SinhVien layChiTiet(String maSV) {
        return sinhVienRepository.findChiTiet(maSV)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên " + maSV + "."));
    }

    public boolean tonTai(String maSV) {
        return sinhVienRepository.existsById(TimKiem.ma(maSV));
    }

    public SinhVienForm layForm(String maSV) {
        SinhVien sv = layChiTiet(maSV);
        SinhVienForm form = new SinhVienForm();
        form.setMaSV(sv.getMaSV());
        form.setHoTen(sv.getHoTen());
        form.setNgaySinh(sv.getNgaySinh());
        form.setGioiTinh(sv.getGioiTinh());
        form.setMaKhoa(sv.getLop().getKhoa().getMaKhoa());
        form.setMaLop(sv.getLop().getMaLop());
        form.setTaoTaiKhoan(false);
        form.setEditMode(true);
        return form;
    }

    /** Thêm mới (kèm tạo tài khoản nếu được chọn) hoặc cập nhật sinh viên. */
    @Transactional
    public String luu(SinhVienForm form) {
        String ma = TimKiem.ma(form.getMaSV());
        Lop lop = lopRepository.findById(form.getMaLop())
                .orElseThrow(() -> new BusinessException("Lớp đã chọn không tồn tại."));
        // QĐ1: lớp phải thuộc khoa đã chọn
        if (!lop.getKhoa().getMaKhoa().equals(form.getMaKhoa())) {
            throw new BusinessException("Lớp đã chọn không thuộc khoa đã chọn.");
        }
        SinhVien sv;
        if (form.isEditMode()) {
            sv = sinhVienRepository.findById(ma)
                    .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên " + ma + "."));
        } else {
            if (sinhVienRepository.existsById(ma)) {
                throw new BusinessException("Mã " + ma + " đã tồn tại.");
            }
            sv = new SinhVien();
            sv.setMaSV(ma);
        }
        sv.setHoTen(form.getHoTen().trim());
        sv.setNgaySinh(form.getNgaySinh());
        sv.setGioiTinh(form.getGioiTinh() == null || form.getGioiTinh().isBlank() ? null : form.getGioiTinh());
        sv.setLop(lop);
        sv = sinhVienRepository.save(sv);

        if (!form.isEditMode() && form.isTaoTaiKhoan()) {
            taiKhoanService.taoChoSinhVien(sv);
        }
        return sv.getHoTen();
    }

    /** Xóa sinh viên chưa có đăng ký, kèm tài khoản gắn với sinh viên đó. */
    @Transactional
    public String xoa(String maSV) {
        SinhVien sv = sinhVienRepository.findById(maSV)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên " + maSV + "."));
        if (dangKyRepository.countBySinhVien(maSV) > 0) {
            throw new BusinessException("Không thể xóa: sinh viên " + sv.getHoTen()
                    + " đang được sử dụng (đã có đăng ký môn học).");
        }
        taiKhoanRepository.findBySinhVien(maSV).ifPresent(taiKhoanRepository::delete);
        sinhVienRepository.delete(sv);
        return sv.getHoTen();
    }

    @Transactional
    public String capTaiKhoan(String maSV) {
        SinhVien sv = sinhVienRepository.findById(maSV)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sinh viên " + maSV + "."));
        return taiKhoanService.taoChoSinhVien(sv);
    }
}
