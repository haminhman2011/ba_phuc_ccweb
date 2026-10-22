package com.dangkymonhoc.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.dto.TaiKhoanForm;
import com.dangkymonhoc.entity.GiangVien;
import com.dangkymonhoc.entity.Quyen;
import com.dangkymonhoc.entity.SinhVien;
import com.dangkymonhoc.entity.TaiKhoan;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.GiangVienRepository;
import com.dangkymonhoc.repository.QuyenRepository;
import com.dangkymonhoc.repository.SinhVienRepository;
import com.dangkymonhoc.repository.TaiKhoanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaiKhoanService {

    public static final String MAT_KHAU_MAC_DINH = "123456";

    private final TaiKhoanRepository taiKhoanRepository;
    private final QuyenRepository quyenRepository;
    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    /** @param trangThai "1" hoạt động, "0" bị khóa, còn lại là tất cả */
    public Page<TaiKhoan> timKiem(String maQuyen, String trangThai, String q, int page) {
        int tt = "1".equals(trangThai) ? 1 : "0".equals(trangThai) ? 0 : -1;
        PageRequest pageable = PageRequest.of(Math.max(page, 0), appProperties.getPageSize(),
                Sort.by("tenDangNhap"));
        Page<TaiKhoan> ds = taiKhoanRepository.timKiem(TimKiem.loc(maQuyen), tt, TimKiem.like(q), pageable);
        if (ds.isEmpty() && ds.getTotalPages() > 0 && page >= ds.getTotalPages()) {
            return timKiem(maQuyen, trangThai, q, ds.getTotalPages() - 1);
        }
        return ds;
    }

    public List<Quyen> dsQuyen() {
        return quyenRepository.findAllSorted();
    }

    public List<SinhVien> sinhVienChuaCoTaiKhoan() {
        return sinhVienRepository.findChuaCoTaiKhoan();
    }

    public List<GiangVien> giangVienChuaCoTaiKhoan() {
        return giangVienRepository.findChuaCoTaiKhoan();
    }

    public Set<String> maSVCoTaiKhoan(Collection<String> dsMaSV) {
        return dsMaSV.isEmpty() ? Set.of() : new HashSet<>(taiKhoanRepository.findMaSVCoTaiKhoan(dsMaSV));
    }

    public Set<String> maGVCoTaiKhoan(Collection<String> dsMaGV) {
        return dsMaGV.isEmpty() ? Set.of() : new HashSet<>(taiKhoanRepository.findMaGVCoTaiKhoan(dsMaGV));
    }

    public boolean tonTai(String tenDangNhap) {
        return tenDangNhap != null && taiKhoanRepository.existsById(tenDangNhap.trim());
    }

    @Transactional
    public void them(TaiKhoanForm form) {
        String ten = form.getTenDangNhap().trim();
        if (taiKhoanRepository.existsById(ten)) {
            throw new BusinessException("Tên đăng nhập " + ten + " đã tồn tại.");
        }
        if (!form.getMatKhau().equals(form.getNhapLaiMatKhau())) {
            throw new BusinessException("Mật khẩu nhập lại không khớp.");
        }
        Quyen quyen = quyenRepository.findById(form.getMaQuyen())
                .orElseThrow(() -> new BusinessException("Quyền không hợp lệ."));
        TaiKhoan tk = new TaiKhoan(ten, passwordEncoder.encode(form.getMatKhau()), true, quyen, null, null);

        // QĐ9: tài khoản SV gắn với đúng một SV, tài khoản GV gắn với đúng một GV
        if (Quyen.SV.equals(quyen.getMaQuyen())) {
            SinhVien sv = sinhVienRepository.findById(blankToEmpty(form.getMaSV()))
                    .orElseThrow(() -> new BusinessException("Vui lòng chọn sinh viên sở hữu tài khoản."));
            if (taiKhoanRepository.findBySinhVien(sv.getMaSV()).isPresent()) {
                throw new BusinessException("Sinh viên " + sv.getHoTen() + " đã có tài khoản.");
            }
            tk.setSinhVien(sv);
        } else if (Quyen.GV.equals(quyen.getMaQuyen())) {
            GiangVien gv = giangVienRepository.findById(blankToEmpty(form.getMaGV()))
                    .orElseThrow(() -> new BusinessException("Vui lòng chọn giảng viên sở hữu tài khoản."));
            if (taiKhoanRepository.findByGiangVien(gv.getMaGV()).isPresent()) {
                throw new BusinessException("Giảng viên " + gv.getHoTen() + " đã có tài khoản.");
            }
            tk.setGiangVien(gv);
        }
        taiKhoanRepository.save(tk);
    }

    /** Tạo tài khoản mặc định cho sinh viên: tên đăng nhập = mã SV viết thường, mật khẩu 123456. */
    @Transactional
    public String taoChoSinhVien(SinhVien sv) {
        if (taiKhoanRepository.findBySinhVien(sv.getMaSV()).isPresent()) {
            throw new BusinessException("Sinh viên " + sv.getHoTen() + " đã có tài khoản.");
        }
        String ten = sv.getMaSV().toLowerCase();
        if (taiKhoanRepository.existsById(ten)) {
            throw new BusinessException("Tên đăng nhập " + ten + " đã tồn tại.");
        }
        taiKhoanRepository.save(new TaiKhoan(ten, passwordEncoder.encode(MAT_KHAU_MAC_DINH), true,
                quyenRepository.getReferenceById(Quyen.SV), sv, null));
        return ten;
    }

    /** Tạo tài khoản mặc định cho giảng viên: tên đăng nhập = mã GV viết thường, mật khẩu 123456. */
    @Transactional
    public String taoChoGiangVien(GiangVien gv) {
        if (taiKhoanRepository.findByGiangVien(gv.getMaGV()).isPresent()) {
            throw new BusinessException("Giảng viên " + gv.getHoTen() + " đã có tài khoản.");
        }
        String ten = gv.getMaGV().toLowerCase();
        if (taiKhoanRepository.existsById(ten)) {
            throw new BusinessException("Tên đăng nhập " + ten + " đã tồn tại.");
        }
        taiKhoanRepository.save(new TaiKhoan(ten, passwordEncoder.encode(MAT_KHAU_MAC_DINH), true,
                quyenRepository.getReferenceById(Quyen.GV), null, gv));
        return ten;
    }

    /** Khóa nếu đang hoạt động, mở khóa nếu đang bị khóa. Trả về trạng thái mới. */
    @Transactional
    public boolean doiTrangThai(String tenDangNhap, String nguoiThucHien) {
        if (tenDangNhap.equals(nguoiThucHien)) {
            throw new BusinessException("Bạn không thể tự khóa tài khoản của mình.");
        }
        TaiKhoan tk = tim(tenDangNhap);
        tk.setTrangThai(!Boolean.TRUE.equals(tk.getTrangThai()));
        return tk.getTrangThai();
    }

    @Transactional
    public void datLaiMatKhau(String tenDangNhap) {
        tim(tenDangNhap).setMatKhau(passwordEncoder.encode(MAT_KHAU_MAC_DINH));
    }

    @Transactional
    public void xoa(String tenDangNhap, String nguoiThucHien) {
        if (tenDangNhap.equals(nguoiThucHien)) {
            throw new BusinessException("Không thể xóa tài khoản đang đăng nhập.");
        }
        TaiKhoan tk = tim(tenDangNhap);
        if (tk.getSinhVien() != null || tk.getGiangVien() != null) {
            throw new BusinessException("Không thể xóa: tài khoản " + tenDangNhap
                    + " đang được sử dụng (gắn với sinh viên/giảng viên).");
        }
        taiKhoanRepository.delete(tk);
    }

    public boolean dungMatKhau(String tenDangNhap, String matKhau) {
        return passwordEncoder.matches(matKhau, tim(tenDangNhap).getMatKhau());
    }

    @Transactional
    public void doiMatKhau(String tenDangNhap, String matKhauMoi) {
        tim(tenDangNhap).setMatKhau(passwordEncoder.encode(matKhauMoi));
    }

    private TaiKhoan tim(String tenDangNhap) {
        return taiKhoanRepository.findById(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản " + tenDangNhap + "."));
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
