package com.dangkymonhoc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dangkymonhoc.entity.LichHoc;

public interface LichHocRepository extends JpaRepository<LichHoc, Integer> {

    @Query("""
            select l from LichHoc l join fetch l.phanCong p join fetch p.giangVien join fetch p.monHoc
                join fetch l.phong
            where p.id.hocKy = :hocKy
            order by l.thu, l.tietBatDau, p.id.maGV, p.id.maMH""")
    List<LichHoc> findByHocKy(@Param("hocKy") String hocKy);

    @Query("""
            select l from LichHoc l join fetch l.phanCong p join fetch p.giangVien join fetch p.monHoc
                join fetch l.phong
            where p.id.maGV = :maGV and p.id.maMH = :maMH and p.id.hocKy = :hocKy""")
    Optional<LichHoc> findByPhanCong(@Param("maGV") String maGV, @Param("maMH") String maMH,
                                     @Param("hocKy") String hocKy);

    @Query("select count(l) from LichHoc l where l.phanCong.id.hocKy = :hocKy and l.trangThai = 'CHINH_THUC'")
    long countChinhThuc(@Param("hocKy") String hocKy);

    /** Tuần cuối cùng có lịch học trong học kỳ, null nếu chưa xếp lịch. */
    @Query("select max(l.tuanKetThuc) from LichHoc l where l.phanCong.id.hocKy = :hocKy")
    Integer maxTuanKetThuc(@Param("hocKy") String hocKy);

    /** Buổi tạo trước khi có cột TuanBatDau/TuanKetThuc (CSDL cũ, cột mới nhận giá trị 0). */
    @Query("""
            select l from LichHoc l join fetch l.phanCong p join fetch p.monHoc
            where l.tuanBatDau is null or l.tuanBatDau < 1""")
    List<LichHoc> findChuaCoTuan();

    @Query("""
            select count(l) from LichHoc l
            where l.phanCong.id.maGV = :maGV and l.phanCong.id.maMH = :maMH and l.phanCong.id.hocKy = :hocKy
              and l.trangThai = 'CHINH_THUC'""")
    long countChinhThucCuaPhanCong(@Param("maGV") String maGV, @Param("maMH") String maMH,
                                   @Param("hocKy") String hocKy);

    /** MH21, QĐ10: lịch chính thức của các lớp sinh viên đã đăng ký trong học kỳ. */
    @Query("""
            select l from LichHoc l join fetch l.phanCong p join fetch p.giangVien join fetch p.monHoc
                join fetch l.phong
            where p.id.hocKy = :hocKy and l.trangThai = 'CHINH_THUC'
              and exists (select d from DangKy d
                          where d.id.maSV = :maSV and d.maGV = p.id.maGV
                            and d.id.maMH = p.id.maMH and d.id.hocKy = p.id.hocKy)
            order by l.thu, l.tietBatDau""")
    List<LichHoc> findChinhThucCuaSinhVien(@Param("maSV") String maSV, @Param("hocKy") String hocKy);

    /** MH22: lịch dạy chính thức của giảng viên trong học kỳ. */
    @Query("""
            select l from LichHoc l join fetch l.phanCong p join fetch p.giangVien join fetch p.monHoc
                join fetch l.phong
            where p.id.maGV = :maGV and p.id.hocKy = :hocKy and l.trangThai = 'CHINH_THUC'
            order by l.thu, l.tietBatDau""")
    List<LichHoc> findChinhThucCuaGiangVien(@Param("maGV") String maGV, @Param("hocKy") String hocKy);
}
