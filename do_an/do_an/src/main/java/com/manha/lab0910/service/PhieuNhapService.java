package com.manha.lab0910.service;

import com.manha.lab0910.entity.*;
import com.manha.lab0910.form.DongNhapForm;
import com.manha.lab0910.form.PhieuNhapForm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * (UC3) Nghiệp vụ nhập kho theo lô.
 */
@Service
public class PhieuNhapService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Sinh mã phiếu kế tiếp dạng PN-0001, PN-0002...
     * Vì số được đệm 0 cho đủ 4 chữ số nên so sánh chuỗi cũng ra đúng thứ tự,
     * lấy MAX là được mã lớn nhất.
     */
    public String sinhMaPhieuMoi() {
        String maLonNhat = em.createQuery(
                        "SELECT MAX(p.maPhieu) FROM PhieuNhap p", String.class)
                .getSingleResult();

        int so = 0;
        if (maLonNhat != null && maLonNhat.startsWith("PN-")) {
            try {
                so = Integer.parseInt(maLonNhat.substring(3));
            } catch (NumberFormatException e) {
                so = 0;   // mã cũ không đúng định dạng thì bỏ qua, đếm lại từ đầu
            }
        }
        return String.format("PN-%04d", so + 1);
    }

    // (luồng phụ 4b) Mã lô phải là duy nhất trong toàn bộ kho
    public boolean maLoDaTonTai(String maLo) {
        Long soLo = em.createQuery(
                        "SELECT COUNT(l) FROM LoHang l WHERE l.maLo = :maLo", Long.class)
                .setParameter("maLo", maLo)
                .getSingleResult();
        return soLo > 0;
    }

    /**
     * Lưu phiếu nhập: tạo 1 phieu_nhap, và với mỗi dòng thì tạo 1 lo_hang + 1 chi_tiet_nhap.
     *
     * @Transactional bọc cả khối: nếu dòng thứ 3 lỗi thì 2 dòng trước cũng bị huỷ,
     * không để kho dính dữ liệu nhập dở (yêu cầu toàn vẹn giao dịch, mục 7 đặc tả).
     */
    @Transactional
    public PhieuNhap luuPhieuNhap(PhieuNhapForm form, Long nguoiNhapId) {
        PhieuNhap phieu = new PhieuNhap();
        phieu.setMaPhieu(sinhMaPhieuMoi());
        phieu.setNgayNhap(LocalDateTime.now());
        // getReference chỉ tạo tham chiếu theo id, không cần chạy SELECT lấy cả bản ghi
        phieu.setNguoiNhap(em.getReference(NguoiDung.class, nguoiNhapId));
        phieu.setNhaCungCap(rong(form.getNhaCungCap()) ? null : form.getNhaCungCap().trim());
        phieu.setGhiChu(rong(form.getGhiChu()) ? null : form.getGhiChu().trim());
        em.persist(phieu);

        for (DongNhapForm dong : form.getChiTiet()) {
            LoHang lo = new LoHang();
            lo.setMaLo(dong.getMaLo().trim());
            lo.setSanPham(em.getReference(SanPham.class, dong.getSanPhamId()));
            if (dong.getViTriId() != null) {
                lo.setViTri(em.getReference(ViTriKho.class, dong.getViTriId()));
            }
            lo.setNgaySanXuat(dong.getNgaySanXuat());
            lo.setHanSuDung(dong.getHanSuDung());
            lo.setSoLuongNhap(dong.getSoLuong());
            lo.setSoLuongTon(dong.getSoLuong());   // lô mới nhập thì tồn = số lượng nhập
            lo.setNgayTao(LocalDateTime.now());
            em.persist(lo);

            ChiTietNhap chiTiet = new ChiTietNhap();
            chiTiet.setPhieuNhap(phieu);
            chiTiet.setLoHang(lo);
            chiTiet.setSoLuong(dong.getSoLuong());
            chiTiet.setDonGia(dong.getDonGia() != null ? dong.getDonGia() : BigDecimal.ZERO);
            em.persist(chiTiet);
        }

        return phieu;
    }

    private boolean rong(String s) {
        return s == null || s.isBlank();
    }
}
