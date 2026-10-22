package com.dangkymonhoc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.PhanCongId;

public interface PhanCongRepository extends JpaRepository<PhanCong, PhanCongId> {

    @Query("""
            select p from PhanCong p join fetch p.giangVien g join fetch p.monHoc m join fetch m.khoa
            where p.id.hocKy = :hocKy
            order by m.maMH, g.maGV""")
    List<PhanCong> findByHocKy(@Param("hocKy") String hocKy);

    @Query("""
            select p from PhanCong p join fetch p.monHoc m
            where p.id.maGV = :maGV and p.id.hocKy = :hocKy
            order by m.maMH""")
    List<PhanCong> findByGiangVienAndHocKy(@Param("maGV") String maGV, @Param("hocKy") String hocKy);

    @Query("select distinct p.id.hocKy from PhanCong p")
    List<String> findAllHocKy();

    @Query("select distinct p.id.hocKy from PhanCong p where p.id.maGV = :maGV")
    List<String> findHocKyByGiangVien(@Param("maGV") String maGV);

    /** QĐ3: môn có ít nhất một phân công trong học kỳ thì được mở. */
    @Query("select count(p) from PhanCong p where p.id.maMH = :maMH and p.id.hocKy = :hocKy")
    long countByMonHocAndHocKy(@Param("maMH") String maMH, @Param("hocKy") String hocKy);

    @Query("select count(p) from PhanCong p where p.id.maMH = :maMH")
    long countByMonHoc(@Param("maMH") String maMH);

    @Query("select count(p) from PhanCong p where p.id.maGV = :maGV")
    long countByGiangVien(@Param("maGV") String maGV);

    @Query("select count(p) from PhanCong p where p.id.maGV = :maGV and p.id.hocKy = :hocKy")
    long countByGiangVienAndHocKy(@Param("maGV") String maGV, @Param("hocKy") String hocKy);

    /** Mỗi phần tử: [maGV, số môn được phân công trong học kỳ]. */
    @Query("select p.id.maGV, count(p) from PhanCong p where p.id.hocKy = :hocKy group by p.id.maGV")
    List<Object[]> countTheoGiangVien(@Param("hocKy") String hocKy);

    @Query("select distinct p.id.maMH from PhanCong p where p.id.hocKy = :hocKy")
    List<String> findMaMonDuocMo(@Param("hocKy") String hocKy);

    @Query("select count(distinct p.id.maMH) from PhanCong p where p.id.hocKy = :hocKy")
    long countMonDuocMo(@Param("hocKy") String hocKy);

    @Query("""
            select distinct m from PhanCong p join p.monHoc m
            where p.id.hocKy = :hocKy
              and not exists (select d from DangKy d where d.monHoc = m and d.id.hocKy = :hocKy)
            order by m.maMH""")
    List<MonHoc> findMonMoChuaCoDangKy(@Param("hocKy") String hocKy);
}
