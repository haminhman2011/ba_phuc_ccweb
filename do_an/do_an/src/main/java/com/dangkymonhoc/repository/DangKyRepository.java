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
            select d from DangKy d join fetch d.monHoc m left join fetch d.phanCong p left join fetch p.giangVien
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

    /** Mỗi phần tử: [maGV, maMH, số SV đăng ký, số SV đã có điểm] của từng lớp trong học kỳ. */
    @Query("""
            select d.maGV, d.id.maMH, count(d), count(d.diem) from DangKy d
            where d.id.hocKy = :hocKy and d.maGV is not null
            group by d.maGV, d.id.maMH""")
    List<Object[]> thongKeTheoLop(@Param("hocKy") String hocKy);

    /** Mỗi phần tử: [maGV, maMH, maSV], dùng để kiểm tra sinh viên học cả hai lớp khi xếp lịch. */
    @Query("select d.maGV, d.id.maMH, d.id.maSV from DangKy d where d.id.hocKy = :hocKy and d.maGV is not null")
    List<Object[]> findSinhVienTheoLop(@Param("hocKy") String hocKy);

    /** Sĩ số lớp: số sinh viên đã đăng ký môn với giảng viên này trong học kỳ. */
    @Query("select count(d) from DangKy d where d.maGV = :maGV and d.id.maMH = :maMH and d.id.hocKy = :hocKy")
    long countByLop(@Param("maGV") String maGV, @Param("maMH") String maMH, @Param("hocKy") String hocKy);

    /** Đăng ký tạo trước khi có cột MaGV (CSDL cũ), cần gán lớp. */
    @Query("select d from DangKy d where d.maGV is null")
    List<DangKy> findChuaCoLop();

    // ----- Phía giảng viên -----

    @Query("""
            select d from DangKy d join fetch d.sinhVien s join fetch s.lop
            where d.maGV = :maGV and d.id.maMH = :maMH and d.id.hocKy = :hocKy
            order by s.maSV""")
    List<DangKy> findDanhSachLop(@Param("maGV") String maGV, @Param("maMH") String maMH,
                                 @Param("hocKy") String hocKy);

    @Query("""
            select count(d) from DangKy d
            where d.id.hocKy = :hocKy and d.maGV = :maGV""")
    long countSinhVienCuaGiangVien(@Param("maGV") String maGV, @Param("hocKy") String hocKy);

    @Query("""
            select count(d) from DangKy d
            where d.id.hocKy = :hocKy and d.maGV = :maGV and d.diem is null""")
    long countChuaCoDiemCuaGiangVien(@Param("maGV") String maGV, @Param("hocKy") String hocKy);
}
