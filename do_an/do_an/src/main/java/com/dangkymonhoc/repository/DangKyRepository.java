package com.dangkymonhoc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dangkymonhoc.dto.TopMonRow;
import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.entity.DangKyId;

public interface DangKyRepository extends JpaRepository<DangKy, DangKyId> {

    // ----- Phía sinh viên -----

    /** Tổng tín chỉ đã đăng ký trong học kỳ; null nếu chưa đăng ký môn nào. */
    @Query("""
            select sum(m.soTinChi) from DangKy d join d.monHoc m
            where d.id.maSV = :maSV and d.id.hocKy = :hocKy""")
    Long tongTinChi(@Param("maSV") String maSV, @Param("hocKy") String hocKy);

    @Query("""
            select d from DangKy d join fetch d.monHoc m
            where d.id.maSV = :maSV and d.id.maMH = :maMH and d.id.hocKy = :hocKy""")
    Optional<DangKy> findChiTiet(@Param("maSV") String maSV, @Param("maMH") String maMH,
                                 @Param("hocKy") String hocKy);

    @Query("""
            select d from DangKy d join fetch d.monHoc m
            where d.id.maSV = :maSV and d.id.hocKy = :hocKy
            order by m.maMH""")
    List<DangKy> findBySinhVienAndHocKy(@Param("maSV") String maSV, @Param("hocKy") String hocKy);

    @Query("""
            select d from DangKy d join fetch d.monHoc m
            where d.id.maSV = :maSV
            order by m.maMH""")
    List<DangKy> findBySinhVien(@Param("maSV") String maSV);

    @Query("select distinct d.id.hocKy from DangKy d where d.id.maSV = :maSV")
    List<String> findHocKyBySinhVien(@Param("maSV") String maSV);

    @Query("select count(d) from DangKy d where d.id.maSV = :maSV and d.diem is not null")
    long countCoDiemBySinhVien(@Param("maSV") String maSV);

    @Query("select count(d) from DangKy d where d.id.maSV = :maSV")
    long countBySinhVien(@Param("maSV") String maSV);

    // ----- Phía môn học / thống kê -----

    @Query("select count(d) from DangKy d where d.id.maMH = :maMH")
    long countByMonHoc(@Param("maMH") String maMH);

    @Query("select count(d) from DangKy d where d.id.maMH = :maMH and d.id.hocKy = :hocKy")
    long countByMonHocAndHocKy(@Param("maMH") String maMH, @Param("hocKy") String hocKy);

    @Query("select count(d) from DangKy d where d.id.hocKy = :hocKy")
    long countByHocKy(@Param("hocKy") String hocKy);

    @Query("""
            select new com.dangkymonhoc.dto.TopMonRow(m.maMH, m.tenMH, count(d))
            from DangKy d join d.monHoc m
            where d.id.hocKy = :hocKy
            group by m.maMH, m.tenMH
            order by count(d) desc, m.maMH""")
    List<TopMonRow> topMon(@Param("hocKy") String hocKy, Pageable pageable);

    /** Mỗi phần tử: [maMH, số SV đăng ký, số SV đã có điểm]. */
    @Query("""
            select d.id.maMH, count(d), count(d.diem) from DangKy d
            where d.id.hocKy = :hocKy
            group by d.id.maMH""")
    List<Object[]> thongKeTheoMon(@Param("hocKy") String hocKy);

    // ----- Phía giảng viên -----

    @Query("""
            select d from DangKy d join fetch d.sinhVien s join fetch s.lop
            where d.id.maMH = :maMH and d.id.hocKy = :hocKy
            order by s.maSV""")
    List<DangKy> findDanhSachLop(@Param("maMH") String maMH, @Param("hocKy") String hocKy);

    @Query("""
            select count(d) from DangKy d
            where d.id.hocKy = :hocKy and d.id.maMH in
                (select p.id.maMH from PhanCong p where p.id.maGV = :maGV and p.id.hocKy = :hocKy)""")
    long countSinhVienCuaGiangVien(@Param("maGV") String maGV, @Param("hocKy") String hocKy);

    @Query("""
            select count(d) from DangKy d
            where d.id.hocKy = :hocKy and d.diem is null and d.id.maMH in
                (select p.id.maMH from PhanCong p where p.id.maGV = :maGV and p.id.hocKy = :hocKy)""")
    long countChuaCoDiemCuaGiangVien(@Param("maGV") String maGV, @Param("hocKy") String hocKy);
}
