package com.manha.lab0910.service;

import com.manha.lab0910.entity.SanPham;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * (UC2) Nghiệp vụ quản lý sản phẩm.
 * Controller chỉ lo nhận request và trả view, mọi quy tắc và câu truy vấn nằm ở đây.
 */
@Service
public class SanPhamService {

    @PersistenceContext
    private EntityManager em;

    private final LoHangService loHangService;

    public SanPhamService(LoHangService loHangService) {
        this.loHangService = loHangService;
    }

    /**
     * Không nhập từ khoá thì trả về toàn bộ danh sách,
     * có từ khoá thì tìm theo mã hoặc tên, không phân biệt hoa thường.
     */
    public List<SanPham> timKiem(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.isBlank()) {
            return em.createQuery(
                            "SELECT s FROM SanPham s ORDER BY s.maSanPham ASC", SanPham.class)
                    .getResultList();
        }

        return em.createQuery(
                        "SELECT s FROM SanPham s " +
                        "WHERE LOWER(s.maSanPham)  LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
                        "   OR LOWER(s.tenSanPham) LIKE LOWER(CONCAT('%', :tuKhoa, '%')) " +
                        "ORDER BY s.maSanPham ASC", SanPham.class)
                .setParameter("tuKhoa", tuKhoa.trim())
                .getResultList();
    }

    // Đổ vào ô chọn sản phẩm ở màn hình nhập kho.
    // Chỉ lấy sản phẩm đang kinh doanh, hàng đã ngừng thì không cho nhập thêm.
    public List<SanPham> laySanPhamDangKinhDoanh() {
        return em.createQuery(
                        "SELECT s FROM SanPham s WHERE s.trangThai = 1 ORDER BY s.maSanPham ASC",
                        SanPham.class)
                .getResultList();
    }

    // Dùng khi nhận id từ form gửi lên: phải chắc sản phẩm còn tồn tại
    // thì mới lưu, tránh gãy khoá ngoại (ví dụ ô chọn bị cũ, sản phẩm đã bị xoá).
    public boolean tonTai(Long id) {
        if (id == null) {
            return false;
        }
        Long dem = em.createQuery(
                        "SELECT COUNT(s) FROM SanPham s WHERE s.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return dem > 0;
    }

    public SanPham timTheoId(Long id) {
        SanPham sanPham = em.find(SanPham.class, id);
        if (sanPham == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm có id = " + id);
        }
        return sanPham;
    }

    /**
     * Mã sản phẩm đã có người khác dùng chưa.
     * Khi sửa thì bỏ qua chính bản ghi đang sửa, nếu không sẽ tự báo trùng với chính nó.
     */
    public boolean maDaTonTai(String maSanPham, Long idDangSua) {
        List<SanPham> ketQua = em.createQuery(
                        "SELECT s FROM SanPham s WHERE s.maSanPham = :ma", SanPham.class)
                .setParameter("ma", maSanPham)
                .getResultList();

        if (ketQua.isEmpty()) {
            return false;
        }
        return idDangSua == null || !ketQua.get(0).getId().equals(idDangSua);
    }

    /**
     * Thêm mới thì persist, sửa thì merge.
     * @Transactional bắt buộc phải có, vì đây là thao tác ghi xuống DB.
     */
    @Transactional
    public SanPham luu(SanPham sanPham) {
        if (sanPham.getId() == null) {
            em.persist(sanPham);
            return sanPham;
        }
        return em.merge(sanPham);
    }

    /**
     * (UC2 - luồng xoá) Chỉ xoá được sản phẩm chưa dính tới lô hàng nào.
     * Còn hàng trong kho hoặc đã có lịch sử nhập/xuất thì phải dùng "Ngừng kinh doanh".
     */
    @Transactional
    public void xoa(Long id) {
        SanPham sanPham = timTheoId(id);

        int tongTon = loHangService.tongTonTheoSanPham(id);
        if (tongTon > 0) {
            throw new IllegalStateException(
                    "Không thể xoá: sản phẩm còn hàng trong kho (còn " + tongTon + " " + sanPham.getDonViTinh()
                    + "). Hãy chuyển sang Ngừng kinh doanh.");
        }

        // Hết tồn nhưng lô cũ vẫn nằm trong lịch sử nhập/xuất -> giữ lại sản phẩm
        if (loHangService.coLoHangCuaSanPham(id)) {
            throw new IllegalStateException(
                    "Không thể xoá: sản phẩm đã có lịch sử nhập/xuất. Hãy chuyển sang Ngừng kinh doanh.");
        }

        em.remove(sanPham);
    }

    // Ngừng kinh doanh / mở bán lại: đổi trang_thai thay vì xoá cứng
    @Transactional
    public SanPham doiTrangThai(Long id) {
        SanPham sanPham = timTheoId(id);
        sanPham.setTrangThai(sanPham.getTrangThai() != null && sanPham.getTrangThai() == 1 ? 0 : 1);
        // Đang trong transaction, sanPham lấy từ em.find() nên Hibernate tự lưu thay đổi,
        // không cần gọi thêm lệnh update nào.
        return sanPham;
    }
}
