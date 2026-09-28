package com.manha.lab0910.service;

import com.manha.lab0910.entity.NguoiDung;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (UC1) Spring Security gọi lớp này để lấy tài khoản từ DB ra.
 * Việc so khớp mật khẩu BCrypt do Spring Security tự làm, mình không cần viết.
 */
@Service
public class NguoiDungChiTietService implements UserDetailsService {

    // EntityManager là "cửa" để nói chuyện với database.
    // Spring tự tiêm vào, mình dùng nó để chạy câu truy vấn JPQL.
    @PersistenceContext
    private EntityManager em;

    @Override
    public UserDetails loadUserByUsername(String tenDangNhap) throws UsernameNotFoundException {
        // JPQL viết theo TÊN LỚP và TÊN THUỘC TÍNH trong Java (NguoiDung, tenDangNhap),
        // không phải tên bảng và tên cột trong MySQL (nguoi_dung, ten_dang_nhap).
        List<NguoiDung> ketQua = em.createQuery(
                        "SELECT n FROM NguoiDung n WHERE n.tenDangNhap = :ten", NguoiDung.class)
                .setParameter("ten", tenDangNhap)
                .getResultList();

        if (ketQua.isEmpty()) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + tenDangNhap);
        }

        return new NguoiDungChiTiet(ketQua.get(0));
    }
}
