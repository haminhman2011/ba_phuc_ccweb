package com.manha.lab0910.service;

import com.manha.lab0910.entity.SanPham;
import com.manha.lab0910.form.DongBaoCaoHuy;
import com.manha.lab0910.form.DongBaoCaoTon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * (UC7) Báo cáo nhập - xuất - tồn theo kỳ, và báo cáo hàng huỷ.
 */
@Service
public class BaoCaoService {

    @PersistenceContext
    private EntityManager em;

    private final SanPhamService sanPhamService;
    private final LoHangService loHangService;

    public BaoCaoService(SanPhamService sanPhamService, LoHangService loHangService) {
        this.sanPhamService = sanPhamService;
        this.loHangService = loHangService;
    }

    /**
     * Báo cáo Nhập - Xuất - Tồn cho khoảng [tuNgay .. denNgay].
     *
     * Database không lưu cột "tồn tại thời điểm X", chỉ có tồn HIỆN TẠI của từng lô.
     * Nên phải tính ngược từ hiện tại đi lùi về:
     *
     *   tồn cuối kỳ = tồn hiện tại - (nhập sau kỳ) + (xuất sau kỳ)
     *   tồn đầu kỳ  = tồn cuối kỳ  - (nhập trong kỳ) + (xuất trong kỳ)
     *
     * Nếu kỳ báo cáo kết thúc đúng hôm nay thì "sau kỳ" bằng 0, tồn cuối = tồn hiện tại.
     */
    public List<DongBaoCaoTon> baoCaoNhapXuatTon(LocalDate tuNgay, LocalDate denNgay) {
        LocalDateTime batDau = tuNgay.atStartOfDay();
        LocalDateTime ketThuc = denNgay.atTime(23, 59, 59);

        List<DongBaoCaoTon> ketQua = new ArrayList<>();

        for (SanPham sp : sanPhamService.timKiem(null)) {
            int tonHienTai = loHangService.tongTonTheoSanPham(sp.getId());

            int nhapTrongKy = tongNhap(sp.getId(), batDau, ketThuc);
            int xuatTrongKy = tongXuat(sp.getId(), batDau, ketThuc);

            // Những gì đã xảy ra SAU kỳ báo cáo, phải trừ ngược ra khỏi tồn hiện tại
            int nhapSauKy = tongNhap(sp.getId(), ketThuc, null);
            int xuatSauKy = tongXuat(sp.getId(), ketThuc, null);

            int tonCuoi = tonHienTai - nhapSauKy + xuatSauKy;
            int tonDau = tonCuoi - nhapTrongKy + xuatTrongKy;

            ketQua.add(new DongBaoCaoTon(sp.getMaSanPham(), sp.getTenSanPham(), sp.getDonViTinh(),
                    tonDau, nhapTrongKy, xuatTrongKy, tonCuoi));
        }

        return ketQua;
    }

    /** Tổng số lượng nhập của 1 sản phẩm trong khoảng thời gian. den = null nghĩa là tới vô cùng. */
    private int tongNhap(Long sanPhamId, LocalDateTime tu, LocalDateTime den) {
        String jpql = "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietNhap ct " +
                      "WHERE ct.loHang.sanPham.id = :spId AND ct.phieuNhap.ngayNhap > :tu";
        if (den != null) {
            jpql = "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietNhap ct " +
                   "WHERE ct.loHang.sanPham.id = :spId " +
                   "AND ct.phieuNhap.ngayNhap >= :tu AND ct.phieuNhap.ngayNhap <= :den";
        }

        var query = em.createQuery(jpql, Long.class)
                .setParameter("spId", sanPhamId)
                .setParameter("tu", tu);
        if (den != null) {
            query.setParameter("den", den);
        }
        return query.getSingleResult().intValue();
    }

    private int tongXuat(Long sanPhamId, LocalDateTime tu, LocalDateTime den) {
        String jpql = "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietXuat ct " +
                      "WHERE ct.loHang.sanPham.id = :spId AND ct.phieuXuat.ngayXuat > :tu";
        if (den != null) {
            jpql = "SELECT COALESCE(SUM(ct.soLuong), 0) FROM ChiTietXuat ct " +
                   "WHERE ct.loHang.sanPham.id = :spId " +
                   "AND ct.phieuXuat.ngayXuat >= :tu AND ct.phieuXuat.ngayXuat <= :den";
        }

        var query = em.createQuery(jpql, Long.class)
                .setParameter("spId", sanPhamId)
                .setParameter("tu", tu);
        if (den != null) {
            query.setParameter("den", den);
        }
        return query.getSingleResult().intValue();
    }

    /** Báo cáo hàng huỷ: các dòng xuất thuộc phiếu có loại XUAT_HUY. */
    public List<DongBaoCaoHuy> baoCaoHangHuy(LocalDate tuNgay, LocalDate denNgay) {
        List<Object[]> dong = em.createQuery(
                        "SELECT ct.loHang.maLo, ct.loHang.sanPham.tenSanPham, ct.soLuong, " +
                        "       ct.loHang.hanSuDung, ct.phieuXuat.ngayXuat, ct.phieuXuat.lyDo " +
                        "FROM ChiTietXuat ct " +
                        "WHERE ct.phieuXuat.loaiXuat = com.manha.lab0910.entity.PhieuXuat$LoaiXuat.XUAT_HUY " +
                        "AND ct.phieuXuat.ngayXuat >= :tu AND ct.phieuXuat.ngayXuat <= :den " +
                        "ORDER BY ct.phieuXuat.ngayXuat DESC", Object[].class)
                .setParameter("tu", tuNgay.atStartOfDay())
                .setParameter("den", denNgay.atTime(23, 59, 59))
                .getResultList();

        List<DongBaoCaoHuy> ketQua = new ArrayList<>();
        for (Object[] o : dong) {
            ketQua.add(new DongBaoCaoHuy(
                    (String) o[0],
                    (String) o[1],
                    ((Number) o[2]).intValue(),
                    (LocalDate) o[3],
                    (LocalDateTime) o[4],
                    (String) o[5]));
        }
        return ketQua;
    }
}
