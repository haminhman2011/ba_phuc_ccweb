package com.dangkymonhoc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dangkymonhoc.entity.MonHoc;

public interface MonHocRepository extends JpaRepository<MonHoc, String> {

    @Query("""
            select m from MonHoc m join fetch m.khoa k
            where (:maKhoa = '' or k.maKhoa = :maKhoa)
              and (lower(m.maMH) like :q or lower(m.tenMH) like :q)
            order by m.maMH""")
    List<MonHoc> timKiem(@Param("maKhoa") String maKhoa, @Param("q") String q);

    @Query("select m from MonHoc m order by m.maMH")
    List<MonHoc> findAllSorted();

    @Query("select count(m) from MonHoc m where m.khoa.maKhoa = :maKhoa")
    long countByKhoa(@Param("maKhoa") String maKhoa);
}
