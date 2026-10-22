package com.dangkymonhoc.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dangkymonhoc.entity.TaiKhoan;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {

    /** Dùng khi đăng nhập và khi cần hiển thị chủ tài khoản. */
    @Query("""
            select t from TaiKhoan t join fetch t.quyen
            left join fetch t.sinhVien left join fetch t.giangVien
            where t.tenDangNhap = :ten""")
    Optional<TaiKhoan> findChiTiet(@Param("ten") String tenDangNhap);

    /**
     * @param maQuyen   chuỗi rỗng = mọi quyền
     * @param trangThai -1 = tất cả, 1 = hoạt động, 0 = bị khóa
     * @param q         chuỗi LIKE đã chuẩn hóa, so với tên đăng nhập và họ tên SV/GV
     */
    @Query(value = """
            select t from TaiKhoan t join fetch t.quyen qu
            left join fetch t.sinhVien s left join fetch t.giangVien g
            where (:maQuyen = '' or qu.maQuyen = :maQuyen)
              and (:trangThai = -1 or (:trangThai = 1 and t.trangThai = true) or (:trangThai = 0 and t.trangThai = false))
              and (lower(t.tenDangNhap) like :q or lower(s.hoTen) like :q or lower(g.hoTen) like :q)""",
           countQuery = """
            select count(t) from TaiKhoan t join t.quyen qu
            left join t.sinhVien s left join t.giangVien g
            where (:maQuyen = '' or qu.maQuyen = :maQuyen)
              and (:trangThai = -1 or (:trangThai = 1 and t.trangThai = true) or (:trangThai = 0 and t.trangThai = false))
              and (lower(t.tenDangNhap) like :q or lower(s.hoTen) like :q or lower(g.hoTen) like :q)""")
    Page<TaiKhoan> timKiem(@Param("maQuyen") String maQuyen, @Param("trangThai") int trangThai,
                           @Param("q") String q, Pageable pageable);

    @Query("select t.sinhVien.maSV from TaiKhoan t where t.sinhVien.maSV in :ds")
    List<String> findMaSVCoTaiKhoan(@Param("ds") Collection<String> dsMaSV);

    @Query("select t.giangVien.maGV from TaiKhoan t where t.giangVien.maGV in :ds")
    List<String> findMaGVCoTaiKhoan(@Param("ds") Collection<String> dsMaGV);

    @Query("select t from TaiKhoan t where t.sinhVien.maSV = :maSV")
    Optional<TaiKhoan> findBySinhVien(@Param("maSV") String maSV);

    @Query("select t from TaiKhoan t where t.giangVien.maGV = :maGV")
    Optional<TaiKhoan> findByGiangVien(@Param("maGV") String maGV);
}
