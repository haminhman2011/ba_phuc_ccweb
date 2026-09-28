package com.example.kholanh.repository;

import com.example.kholanh.entity.LoHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoHangRepository extends JpaRepository<LoHang, Long> {

    // (FR4 - UC4) Lấy các lô còn hàng của 1 sản phẩm, sắp xếp FEFO: hết hạn trước ra trước.
    // Dùng khi xuất kho để trừ dần từ lô sắp hết hạn nhất.
    @Query("SELECT l FROM LoHang l " +
           "WHERE l.sanPham.id = :sanPhamId AND l.soLuongTon > 0 " +
           "ORDER BY l.hanSuDung ASC")
    List<LoHang> timLoTheoFEFO(@Param("sanPhamId") Long sanPhamId);

    // (FR6 - UC6) Các lô SẮP hết hạn: hạn dùng nằm trong khoảng [hôm nay .. hôm nay + ngưỡng].
    @Query("SELECT l FROM LoHang l " +
           "WHERE l.soLuongTon > 0 " +
           "AND l.hanSuDung BETWEEN :tuNgay AND :denNgay " +
           "ORDER BY l.hanSuDung ASC")
    List<LoHang> timLoSapHetHan(@Param("tuNgay") LocalDate tuNgay,
                                @Param("denNgay") LocalDate denNgay);

    // (FR6) Các lô ĐÃ quá hạn nhưng còn tồn (chỉ được xuất huỷ - xem UC4 luồng phụ 4a).
    @Query("SELECT l FROM LoHang l " +
           "WHERE l.soLuongTon > 0 AND l.hanSuDung < :homNay " +
           "ORDER BY l.hanSuDung ASC")
    List<LoHang> timLoQuaHan(@Param("homNay") LocalDate homNay);

    // (FR5) Tổng tồn hiện tại của 1 sản phẩm (cộng dồn các lô).
    @Query("SELECT COALESCE(SUM(l.soLuongTon), 0) FROM LoHang l WHERE l.sanPham.id = :sanPhamId")
    Integer tongTonTheoSanPham(@Param("sanPhamId") Long sanPhamId);
}
