package com.manha.lab0910.service;

import com.manha.lab0910.entity.ViTriKho;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * (5.4) Quản lý vị trí kho. Cách làm giống hệt quản lý sản phẩm.
 */
@Service
public class ViTriKhoService {

    @PersistenceContext
    private EntityManager em;

    private final LoHangService loHangService;

    public ViTriKhoService(LoHangService loHangService) {
        this.loHangService = loHangService;
    }

    // Đổ vào ô chọn vị trí ở màn hình nhập kho
    public List<ViTriKho> layTatCa() {
        return em.createQuery(
                        "SELECT v FROM ViTriKho v ORDER BY v.maViTri ASC", ViTriKho.class)
                .getResultList();
    }

    public List<ViTriKho> timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return layTatCa();
        }
        return em.createQuery(
                        "SELECT v FROM ViTriKho v " +
                        "WHERE LOWER(v.maViTri)  LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
                        "   OR LOWER(v.tenViTri) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
                        "ORDER BY v.maViTri ASC", ViTriKho.class)
                .setParameter("tuKhoa", tuKhoa.trim())
                .getResultList();
    }

    public ViTriKho timTheoId(Long id) {
        ViTriKho viTri = em.find(ViTriKho.class, id);
        if (viTri == null) {
            throw new IllegalArgumentException("Không tìm thấy vị trí có id = " + id);
        }
        return viTri;
    }

    // Vị trí được phép bỏ trống khi nhập kho, nhưng đã chọn thì phải là vị trí có thật
    public boolean tonTai(Long id) {
        if (id == null) {
            return false;
        }
        Long dem = em.createQuery(
                        "SELECT COUNT(v) FROM ViTriKho v WHERE v.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return dem > 0;
    }

    public boolean maDaTonTai(String maViTri, Long idDangSua) {
        List<ViTriKho> ketQua = em.createQuery(
                        "SELECT v FROM ViTriKho v WHERE v.maViTri = :ma", ViTriKho.class)
                .setParameter("ma", maViTri)
                .getResultList();
        if (ketQua.isEmpty()) {
            return false;
        }
        return idDangSua == null || !ketQua.get(0).getId().equals(idDangSua);
    }

    @Transactional
    public ViTriKho luu(ViTriKho viTri) {
        if (viTri.getId() == null) {
            em.persist(viTri);
            return viTri;
        }
        return em.merge(viTri);
    }

    /** Không xoá được vị trí đang chứa hàng, cũng không xoá nếu đã dính vào lô cũ. */
    @Transactional
    public void xoa(Long id) {
        ViTriKho viTri = timTheoId(id);

        int tongTon = loHangService.tongTonTheoViTri(id);
        if (tongTon > 0) {
            throw new IllegalStateException(
                    "Không thể xoá: vị trí đang chứa hàng (còn " + tongTon + " đơn vị)");
        }
        if (loHangService.coLoHangOViTri(id)) {
            throw new IllegalStateException(
                    "Không thể xoá: vị trí đã từng chứa lô hàng, xoá đi là gãy lịch sử nhập/xuất");
        }

        em.remove(viTri);
    }
}
