package com.manha.lab0910.service;

import com.manha.lab0910.entity.ChiTietXuat;
import com.manha.lab0910.entity.LoHang;
import com.manha.lab0910.entity.NguoiDung;
import com.manha.lab0910.entity.PhieuXuat;
import com.manha.lab0910.form.DongPhanBo;
import com.manha.lab0910.form.PhieuXuatForm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * (UC4) Xuất kho theo nguyên tắc FEFO - First Expired, First Out.
 */
@Service
public class PhieuXuatService {

    @PersistenceContext
    private EntityManager em;

    private final LoHangService loHangService;

    public PhieuXuatService(LoHangService loHangService) {
        this.loHangService = loHangService;
    }

    /** Sinh mã phiếu kế tiếp dạng PX-0001, PX-0002... */
    public String sinhMaPhieuMoi() {
        String maLonNhat = em.createQuery(
                        "SELECT MAX(p.maPhieu) FROM PhieuXuat p", String.class)
                .getSingleResult();

        int so = 0;
        if (maLonNhat != null && maLonNhat.startsWith("PX-")) {
            try {
                so = Integer.parseInt(maLonNhat.substring(3));
            } catch (NumberFormatException e) {
                so = 0;
            }
        }
        return String.format("PX-%04d", so + 1);
    }

    /**
     * Tính bảng phân bổ FEFO: cần xuất N thì trừ ở những lô nào, mỗi lô bao nhiêu.
     *
     * Các lô đã được sắp theo hạn dùng tăng dần, nên cứ trừ lần lượt từ đầu danh sách:
     * lô nào hết hạn sớm nhất thì ra trước. Lô không đủ thì trừ hết lô đó rồi tràn sang lô kế.
     *
     * Hàm này KHÔNG ghi gì xuống DB, chỉ tính ra đề xuất để người dùng xem.
     */
    public List<DongPhanBo> tinhPhanBoFEFO(Long sanPhamId, int soLuongCanXuat) {
        LocalDate homNay = LocalDate.now();
        List<DongPhanBo> phanBo = new ArrayList<>();

        int conThieu = soLuongCanXuat;
        for (LoHang lo : loHangService.timLoTheoFEFO(sanPhamId)) {
            if (conThieu <= 0) {
                break;
            }
            // Trừ tối đa bằng số còn tồn của lô này
            int tru = Math.min(conThieu, lo.getSoLuongTon());
            phanBo.add(new DongPhanBo(
                    lo.getId(),
                    lo.getMaLo(),
                    lo.getHanSuDung(),
                    lo.getSoLuongTon(),
                    tru,
                    lo.getHanSuDung().isBefore(homNay)));
            conThieu -= tru;
        }

        return phanBo;
    }

    /** Tổng tồn hiện có của sản phẩm, dùng để chặn xuất quá tồn. */
    public int tongTonHienCo(Long sanPhamId) {
        return loHangService.tongTonTheoSanPham(sanPhamId);
    }

    /**
     * Ghi phiếu xuất và trừ tồn từng lô.
     *
     * Toàn bộ nằm trong một @Transactional: hoặc trừ hết mọi lô và ghi xong phiếu,
     * hoặc lỗi giữa chừng thì rollback sạch, không để kho lệch số (đặc tả mục 6.1).
     *
     * Phân bổ được TÍNH LẠI ở đây chứ không tin bảng đã hiện ở bước 2, vì giữa lúc
     * người dùng xem và lúc bấm xác nhận, tồn kho có thể đã thay đổi.
     */
    @Transactional
    public PhieuXuat luuPhieuXuat(PhieuXuatForm form, Long nguoiXuatId) {
        int tongTon = tongTonHienCo(form.getSanPhamId());
        if (form.getSoLuong() > tongTon) {
            throw new IllegalStateException("Không đủ tồn kho (còn " + tongTon + ")");
        }

        List<DongPhanBo> phanBo = tinhPhanBoFEFO(form.getSanPhamId(), form.getSoLuong());

        // Xuất bán thì không được đụng vào lô quá hạn (đặc tả UC4 - luồng phụ 4a)
        if (form.getLoaiXuat() == PhieuXuat.LoaiXuat.XUAT_BAN) {
            for (DongPhanBo dong : phanBo) {
                if (dong.isQuaHan()) {
                    throw new IllegalStateException("Lô đã quá hạn, chỉ được xuất huỷ");
                }
            }
        }

        PhieuXuat phieu = new PhieuXuat();
        phieu.setMaPhieu(sinhMaPhieuMoi());
        phieu.setNgayXuat(LocalDateTime.now());
        phieu.setNguoiXuat(em.getReference(NguoiDung.class, nguoiXuatId));
        phieu.setLoaiXuat(form.getLoaiXuat());
        phieu.setLyDo(rong(form.getLyDo()) ? null : form.getLyDo().trim());
        phieu.setGhiChu(rong(form.getGhiChu()) ? null : form.getGhiChu().trim());
        em.persist(phieu);

        for (DongPhanBo dong : phanBo) {
            LoHang lo = em.find(LoHang.class, dong.getLoHangId());

            // Kiểm tra lần cuối ngay trước khi trừ, phòng trường hợp tồn vừa bị đổi
            if (lo.getSoLuongTon() < dong.getSoLuongTru()) {
                throw new IllegalStateException(
                        "Tồn của lô " + lo.getMaLo() + " vừa thay đổi, vui lòng tính lại phân bổ");
            }
            lo.setSoLuongTon(lo.getSoLuongTon() - dong.getSoLuongTru());

            ChiTietXuat chiTiet = new ChiTietXuat();
            chiTiet.setPhieuXuat(phieu);
            chiTiet.setLoHang(lo);
            chiTiet.setSoLuong(dong.getSoLuongTru());
            chiTiet.setDonGia(BigDecimal.ZERO);
            em.persist(chiTiet);
        }

        return phieu;
    }

    private boolean rong(String s) {
        return s == null || s.isBlank();
    }
}
