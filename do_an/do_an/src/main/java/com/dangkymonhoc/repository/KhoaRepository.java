package com.dangkymonhoc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dangkymonhoc.dto.KhoaRow;
import com.dangkymonhoc.entity.Khoa;

public interface KhoaRepository extends JpaRepository<Khoa, String> {

    @Query("select k from Khoa k order by k.maKhoa")
    List<Khoa> findAllSorted();

    /** @param q chuỗi LIKE đã chuẩn hóa, ví dụ "%cntt%" */
    @Query("""
            select new com.dangkymonhoc.dto.KhoaRow(k.maKhoa, k.tenKhoa,
                (select count(l) from Lop l where l.khoa = k),
                (select count(g) from GiangVien g where g.khoa = k),
                (select count(m) from MonHoc m where m.khoa = k))
            from Khoa k
            where lower(k.maKhoa) like :q or lower(k.tenKhoa) like :q
            order by k.maKhoa""")
    List<KhoaRow> timKiem(@Param("q") String q);
}
