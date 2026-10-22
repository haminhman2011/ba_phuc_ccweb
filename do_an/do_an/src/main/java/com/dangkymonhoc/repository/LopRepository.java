package com.dangkymonhoc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dangkymonhoc.dto.LopOption;
import com.dangkymonhoc.dto.LopRow;
import com.dangkymonhoc.entity.Lop;

public interface LopRepository extends JpaRepository<Lop, String> {

    /** @param maKhoa chuỗi rỗng = tất cả khoa */
    @Query("""
            select new com.dangkymonhoc.dto.LopRow(l.maLop, l.tenLop, l.khoaHoc, k.maKhoa, k.tenKhoa,
                (select count(s) from SinhVien s where s.lop = l))
            from Lop l join l.khoa k
            where (:maKhoa = '' or k.maKhoa = :maKhoa)
              and (lower(l.maLop) like :q or lower(l.tenLop) like :q)
            order by l.maLop""")
    List<LopRow> timKiem(@Param("maKhoa") String maKhoa, @Param("q") String q);

    @Query("""
            select new com.dangkymonhoc.dto.LopOption(l.maLop, l.tenLop)
            from Lop l where l.khoa.maKhoa = :maKhoa order by l.maLop""")
    List<LopOption> findOptionsByKhoa(@Param("maKhoa") String maKhoa);

    @Query("select count(l) from Lop l where l.khoa.maKhoa = :maKhoa")
    long countByKhoa(@Param("maKhoa") String maKhoa);
}
