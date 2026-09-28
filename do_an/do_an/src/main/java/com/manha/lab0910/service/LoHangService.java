package com.manha.lab0910.service;

import com.manha.lab0910.entity.LoHang;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Các truy vấn liên quan tới lô hàng: FEFO, hạn dùng, tồn kho.
 * Lô hàng là bảng trung tâm nên nhiều màn hình khác sẽ gọi vào đây.
 */
@Service
public class LoHangService {

    @PersistenceContext
    private EntityManager em;

    // (FR4 - UC4) Lấy các lô còn hàng của 1 sản phẩm, sắp xếp FEFO: hết hạn trước ra trước.
    // Dùng khi xuất kho để trừ dần từ lô sắp hết hạn nhất.
    public List<LoHang> timLoTheoFEFO(Long sanPhamId) {
        return em.createQuery(
                        "SELECT l FROM LoHang l " +
                        "WHERE l.sanPham.id = :sanPhamId AND l.soLuongTon > 0 " +
                        "ORDER BY l.hanSuDung ASC", LoHang.class)
                .setParameter("sanPhamId", sanPhamId)
                .getResultList();
    }

    // (FR6 - UC6) Các lô SẮP hết hạn: hạn dùng nằm trong khoảng [hôm nay .. hôm nay + ngưỡng].
    public List<LoHang> timLoSapHetHan(LocalDate tuNgay, LocalDate denNgay) {
        return em.createQuery(
                        "SELECT l FROM LoHang l " +
                        "WHERE l.soLuongTon > 0 " +
                        "AND l.hanSuDung BETWEEN :tuNgay AND :denNgay " +
                        "ORDER BY l.hanSuDung ASC", LoHang.class)
                .setParameter("tuNgay", tuNgay)
                .setParameter("denNgay", denNgay)
                .getResultList();
    }

    // (FR6) Các lô ĐÃ quá hạn nhưng còn tồn (chỉ được xuất huỷ - xem UC4 luồng phụ 4a).
    public List<LoHang> timLoQuaHan(LocalDate homNay) {
        return em.createQuery(
                        "SELECT l FROM LoHang l " +
                        "WHERE l.soLuongTon > 0 AND l.hanSuDung < :homNay " +
                        "ORDER BY l.hanSuDung ASC", LoHang.class)
                .setParameter("homNay", homNay)
                .getResultList();
    }

    // (FR5) Tổng tồn hiện tại của 1 sản phẩm (cộng dồn các lô).
    // Lưu ý: SUM() trong JPQL luôn trả về Long dù cột là INT,
    // nên phải nhận bằng Long rồi mới đổi sang int.
    public int tongTonTheoSanPham(Long sanPhamId) {
        Long tong = em.createQuery(
                        "SELECT COALESCE(SUM(l.soLuongTon), 0) FROM LoHang l " +
                        "WHERE l.sanPham.id = :sanPhamId", Long.class)
                .setParameter("sanPhamId", sanPhamId)
                .getSingleResult();
        return tong.intValue();
    }

    // (5.4 - luồng xoá) Tổng tồn đang nằm ở một vị trí kho.
    public int tongTonTheoViTri(Long viTriId) {
        Long tong = em.createQuery(
                        "SELECT COALESCE(SUM(l.soLuongTon), 0) FROM LoHang l " +
                        "WHERE l.viTri.id = :viTriId", Long.class)
                .setParameter("viTriId", viTriId)
                .getSingleResult();
        return tong.intValue();
    }

    // (5.4 - luồng xoá) Vị trí đã từng được gán cho lô nào chưa.
    public boolean coLoHangOViTri(Long viTriId) {
        Long soLo = em.createQuery(
                        "SELECT COUNT(l) FROM LoHang l WHERE l.viTri.id = :viTriId", Long.class)
                .setParameter("viTriId", viTriId)
                .getSingleResult();
        return soLo > 0;
    }

    // (UC2 - luồng xoá) Sản phẩm đã từng có lô nào chưa. Lô tồn = 0 vẫn phải giữ
    // vì nó nằm trong lịch sử nhập/xuất, xoá sản phẩm đi là gãy khoá ngoại.
    public boolean coLoHangCuaSanPham(Long sanPhamId) {
        Long soLo = em.createQuery(
                        "SELECT COUNT(l) FROM LoHang l WHERE l.sanPham.id = :sanPhamId", Long.class)
                .setParameter("sanPhamId", sanPhamId)
                .getSingleResult();
        return soLo > 0;
    }
}
