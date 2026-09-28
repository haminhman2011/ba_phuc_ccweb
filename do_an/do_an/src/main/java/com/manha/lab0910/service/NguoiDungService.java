package com.manha.lab0910.service;

import com.manha.lab0910.entity.NguoiDung;
import com.manha.lab0910.form.NguoiDungForm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * (UC8) Quản lý tài khoản người dùng.
 */
@Service
public class NguoiDungService {

    @PersistenceContext
    private EntityManager em;

    private final PasswordEncoder passwordEncoder;

    public NguoiDungService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<NguoiDung> layTatCa() {
        return em.createQuery(
                        "SELECT n FROM NguoiDung n ORDER BY n.tenDangNhap ASC", NguoiDung.class)
                .getResultList();
    }

    public NguoiDung timTheoId(Long id) {
        NguoiDung nd = em.find(NguoiDung.class, id);
        if (nd == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản có id = " + id);
        }
        return nd;
    }

    /** Tên đăng nhập đã bị người khác dùng chưa (bỏ qua chính tài khoản đang sửa). */
    public boolean tenDangNhapDaTonTai(String tenDangNhap, Long idDangSua) {
        List<NguoiDung> ketQua = em.createQuery(
                        "SELECT n FROM NguoiDung n WHERE n.tenDangNhap = :ten", NguoiDung.class)
                .setParameter("ten", tenDangNhap)
                .getResultList();

        if (ketQua.isEmpty()) {
            return false;
        }
        return idDangSua == null || !ketQua.get(0).getId().equals(idDangSua);
    }

    @Transactional
    public NguoiDung luu(NguoiDungForm form) {
        if (form.getId() == null) {
            NguoiDung nguoiDung = new NguoiDung();
            nguoiDung.setTenDangNhap(form.getTenDangNhap().trim());
            nguoiDung.setNgayTao(LocalDateTime.now());
            // Mật khẩu luôn được mã hoá BCrypt trước khi lưu, không bao giờ lưu dạng thô
            nguoiDung.setMatKhau(passwordEncoder.encode(form.getMatKhau()));
            nguoiDung.setHoTen(form.getHoTen().trim());
            nguoiDung.setVaiTro(form.getVaiTro());
            nguoiDung.setTrangThai(form.getTrangThai());

            // Khoá chính dùng GenerationType.IDENTITY nên persist() chạy câu INSERT ngay
            // để lấy id do MySQL cấp. Vì vậy phải gán đủ các cột NOT NULL TRƯỚC khi gọi,
            // gán sau thì câu INSERT đã chạy với giá trị null và báo lỗi.
            em.persist(nguoiDung);
            return nguoiDung;
        }

        NguoiDung nguoiDung = timTheoId(form.getId());
        // Tên đăng nhập không cho đổi sau khi tạo, tránh lệch với lịch sử nhập/xuất
        if (form.getMatKhau() != null && !form.getMatKhau().isBlank()) {
            nguoiDung.setMatKhau(passwordEncoder.encode(form.getMatKhau()));
        }
        nguoiDung.setHoTen(form.getHoTen().trim());
        nguoiDung.setVaiTro(form.getVaiTro());
        nguoiDung.setTrangThai(form.getTrangThai());
        // Đang trong transaction, Hibernate tự sinh câu UPDATE khi kết thúc
        return nguoiDung;
    }

    /** Khoá / mở khoá tài khoản. Trả về trạng thái mới. */
    @Transactional
    public NguoiDung doiTrangThai(Long id) {
        NguoiDung nguoiDung = timTheoId(id);
        nguoiDung.setTrangThai(nguoiDung.getTrangThai() != null && nguoiDung.getTrangThai() == 1 ? 0 : 1);
        return nguoiDung;
    }
}
