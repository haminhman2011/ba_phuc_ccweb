package com.manha.lab0910.service;

import com.manha.lab0910.entity.LoHang;
import com.manha.lab0910.entity.SanPham;
import com.manha.lab0910.form.DongTonChiTiet;
import com.manha.lab0910.form.DongTonTongHop;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * (UC5) Tra cứu tồn kho: xem tổng hợp theo sản phẩm hoặc chi tiết theo từng lô.
 * Lớp này cũng cung cấp luôn các con số cho Dashboard.
 */
@Service
public class TonKhoService {

    @PersistenceContext
    private EntityManager em;

    private final SanPhamService sanPhamService;
    private final LoHangService loHangService;

    public TonKhoService(SanPhamService sanPhamService, LoHangService loHangService) {
        this.sanPhamService = sanPhamService;
        this.loHangService = loHangService;
    }

    /** Chế độ xem tổng hợp: mỗi sản phẩm một dòng, cộng dồn tồn của tất cả các lô. */
    public List<DongTonTongHop> xemTongHop(Long sanPhamId) {
        List<DongTonTongHop> ketQua = new ArrayList<>();

        for (SanPham sp : sanPhamService.timKiem(null)) {
            if (sanPhamId != null && !sp.getId().equals(sanPhamId)) {
                continue;
            }
            int tongTon = loHangService.tongTonTheoSanPham(sp.getId());
            ketQua.add(new DongTonTongHop(sp.getMaSanPham(), sp.getTenSanPham(), sp.getDonViTinh(),
                    tongTon, sp.getTonToiThieu() != null ? sp.getTonToiThieu() : 0));
        }
        return ketQua;
    }

    /**
     * Chế độ xem chi tiết: mỗi lô còn tồn một dòng.
     * tinhTrang nhận "SAP_HET_HAN" hoặc "QUA_HAN", để trống là lấy tất cả.
     */
    public List<DongTonChiTiet> xemChiTiet(Long sanPhamId, Long viTriId, String tinhTrang) {
        LocalDate homNay = LocalDate.now();

        // JOIN FETCH vì sanPham và viTri khai LAZY, mà open-in-view đang tắt.
        // Không nạp sẵn thì ra tới template sẽ văng LazyInitializationException.
        StringBuilder jpql = new StringBuilder(
                "SELECT l FROM LoHang l " +
                "JOIN FETCH l.sanPham " +
                "LEFT JOIN FETCH l.viTri " +
                "WHERE l.soLuongTon > 0");

        if (sanPhamId != null) {
            jpql.append(" AND l.sanPham.id = :sanPhamId");
        }
        if (viTriId != null) {
            jpql.append(" AND l.viTri.id = :viTriId");
        }
        if ("QUA_HAN".equals(tinhTrang)) {
            jpql.append(" AND l.hanSuDung < :homNay");
        } else if ("SAP_HET_HAN".equals(tinhTrang)) {
            jpql.append(" AND l.hanSuDung >= :homNay AND l.hanSuDung <= :nguong");
        }
        jpql.append(" ORDER BY l.hanSuDung ASC");

        TypedQuery<LoHang> query = em.createQuery(jpql.toString(), LoHang.class);
        if (sanPhamId != null) {
            query.setParameter("sanPhamId", sanPhamId);
        }
        if (viTriId != null) {
            query.setParameter("viTriId", viTriId);
        }
        if ("QUA_HAN".equals(tinhTrang)) {
            query.setParameter("homNay", homNay);
        } else if ("SAP_HET_HAN".equals(tinhTrang)) {
            query.setParameter("homNay", homNay);
            query.setParameter("nguong", homNay.plusDays(7));
        }

        List<DongTonChiTiet> ketQua = new ArrayList<>();
        for (LoHang lo : query.getResultList()) {
            ketQua.add(taoDong(lo, homNay));
        }
        return ketQua;
    }

    // ===== Số liệu cho Dashboard (5.2) =====

    public long demSanPham() {
        return em.createQuery("SELECT COUNT(s) FROM SanPham s WHERE s.trangThai = 1", Long.class)
                .getSingleResult();
    }

    public int tongTonToanKho() {
        Long tong = em.createQuery(
                        "SELECT COALESCE(SUM(l.soLuongTon), 0) FROM LoHang l", Long.class)
                .getSingleResult();
        return tong.intValue();
    }

    public int demLoSapHetHan() {
        LocalDate homNay = LocalDate.now();
        return loHangService.timLoSapHetHan(homNay, homNay.plusDays(7)).size();
    }

    public int demLoQuaHan() {
        return loHangService.timLoQuaHan(LocalDate.now()).size();
    }

    /** Bảng "Lô cần ưu tiên xử lý" trên dashboard: lô quá hạn và sắp hết hạn, gấp nhất lên đầu. */
    public List<DongTonChiTiet> loCanUuTien(int soLuong) {
        LocalDate homNay = LocalDate.now();

        List<LoHang> dsLo = em.createQuery(
                        "SELECT l FROM LoHang l " +
                        "JOIN FETCH l.sanPham " +
                        "LEFT JOIN FETCH l.viTri " +
                        "WHERE l.soLuongTon > 0 AND l.hanSuDung <= :nguong " +
                        "ORDER BY l.hanSuDung ASC", LoHang.class)
                .setParameter("nguong", homNay.plusDays(7))
                .setMaxResults(soLuong)
                .getResultList();

        List<DongTonChiTiet> ketQua = new ArrayList<>();
        for (LoHang lo : dsLo) {
            ketQua.add(taoDong(lo, homNay));
        }
        return ketQua;
    }

    private DongTonChiTiet taoDong(LoHang lo, LocalDate homNay) {
        return new DongTonChiTiet(
                lo.getMaLo(),
                lo.getSanPham().getTenSanPham(),
                lo.getViTri() != null ? lo.getViTri().getTenViTri() : null,
                lo.getHanSuDung(),
                lo.getSoLuongTon(),
                ChronoUnit.DAYS.between(homNay, lo.getHanSuDung()));
    }
}
