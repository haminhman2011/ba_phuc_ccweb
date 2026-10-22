package com.dangkymonhoc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dangkymonhoc.entity.GiangVien;

public interface GiangVienRepository extends JpaRepository<GiangVien, String> {

    @Query(value = """
            select g from GiangVien g join fetch g.khoa k
            where (:maKhoa = '' or k.maKhoa = :maKhoa)
              and (lower(g.maGV) like :q or lower(g.hoTen) like :q)""",
           countQuery = """
            select count(g) from GiangVien g join g.khoa k
            where (:maKhoa = '' or k.maKhoa = :maKhoa)
              and (lower(g.maGV) like :q or lower(g.hoTen) like :q)""")
    Page<GiangVien> timKiem(@Param("maKhoa") String maKhoa, @Param("q") String q, Pageable pageable);

    @Query("select g from GiangVien g join fetch g.khoa where g.maGV = :maGV")
    Optional<GiangVien> findChiTiet(@Param("maGV") String maGV);

    @Query("select g from GiangVien g join fetch g.khoa order by g.maGV")
    List<GiangVien> findAllWithKhoa();

    @Query("select count(g) from GiangVien g where g.khoa.maKhoa = :maKhoa")
    long countByKhoa(@Param("maKhoa") String maKhoa);

    @Query("""
            select g from GiangVien g
            where not exists (select t from TaiKhoan t where t.giangVien = g)
            order by g.maGV""")
    List<GiangVien> findChuaCoTaiKhoan();
}
