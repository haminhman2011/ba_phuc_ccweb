package com.dangkymonhoc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dangkymonhoc.entity.SinhVien;

public interface SinhVienRepository extends JpaRepository<SinhVien, String> {

    /** maKhoa, maLop: chuỗi rỗng = không lọc; q: chuỗi LIKE đã chuẩn hóa. */
    @Query(value = """
            select s from SinhVien s join fetch s.lop l join fetch l.khoa k
            where (:maKhoa = '' or k.maKhoa = :maKhoa)
              and (:maLop = '' or l.maLop = :maLop)
              and (lower(s.maSV) like :q or lower(s.hoTen) like :q)""",
           countQuery = """
            select count(s) from SinhVien s join s.lop l join l.khoa k
            where (:maKhoa = '' or k.maKhoa = :maKhoa)
              and (:maLop = '' or l.maLop = :maLop)
              and (lower(s.maSV) like :q or lower(s.hoTen) like :q)""")
    Page<SinhVien> timKiem(@Param("maKhoa") String maKhoa, @Param("maLop") String maLop,
                           @Param("q") String q, Pageable pageable);

    @Query("select s from SinhVien s join fetch s.lop l join fetch l.khoa where s.maSV = :maSV")
    Optional<SinhVien> findChiTiet(@Param("maSV") String maSV);

    @Query("select count(s) from SinhVien s where s.lop.maLop = :maLop")
    long countByLop(@Param("maLop") String maLop);

    @Query("""
            select s from SinhVien s
            where not exists (select t from TaiKhoan t where t.sinhVien = s)
            order by s.maSV""")
    List<SinhVien> findChuaCoTaiKhoan();
}
