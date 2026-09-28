package com.manha.lab0910.service;

import com.manha.lab0910.entity.CanhBao;
import com.manha.lab0910.entity.LoHang;
import com.manha.lab0910.entity.SanPham;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * (UC6) Cảnh báo hạn dùng và tồn tối thiểu.
 *
 * Đây là chức năng của tác nhân "Hệ thống (thời gian)": không ai bấm nút,
 * job tự chạy theo lịch rồi ghi bản ghi vào bảng canh_bao.
 */
@Service
public class CanhBaoService {

    private static final Logger log = LoggerFactory.getLogger(CanhBaoService.class);

    @PersistenceContext
    private EntityManager em;

    private final LoHangService loHangService;
    private final SanPhamService sanPhamService;

    // Đọc từ application.properties, mặc định 7 ngày nếu không khai báo
    @Value("${kholanh.canh-bao.nguong-ngay:7}")
    private int nguongNgay;

    public CanhBaoService(LoHangService loHangService, SanPhamService sanPhamService) {
        this.loHangService = loHangService;
        this.sanPhamService = sanPhamService;
    }

    public int getNguongNgay() {
        return nguongNgay;
    }

    /** Job tự động: mặc định 1 giờ sáng mỗi ngày. */
    @Scheduled(cron = "${kholanh.canh-bao.lich:0 0 1 * * *}")
    public void chayTheoLich() {
        int soMoi = quetVaSinhCanhBao();
        log.info("Job quét cảnh báo đã chạy, sinh thêm {} cảnh báo mới", soMoi);
    }

    /**
     * Rà soát toàn bộ kho và sinh cảnh báo. Trả về số cảnh báo MỚI được tạo.
     *
     * Job chạy mỗi ngày nên cùng một lô sẽ bị quét đi quét lại. Nếu cứ thấy là ghi
     * thì sau một tuần có 7 bản ghi giống hệt nhau. Vì vậy trước khi ghi phải kiểm tra
     * đã có cảnh báo cùng loại cho lô/sản phẩm đó mà chưa xử lý hay chưa.
     */
    @Transactional
    public int quetVaSinhCanhBao() {
        LocalDate homNay = LocalDate.now();
        int soMoi = 0;

        // (3) Lô sắp hết hạn: hạn dùng nằm trong khoảng hôm nay .. hôm nay + ngưỡng
        for (LoHang lo : loHangService.timLoSapHetHan(homNay, homNay.plusDays(nguongNgay))) {
            long conLai = java.time.temporal.ChronoUnit.DAYS.between(homNay, lo.getHanSuDung());
            String noiDung = "Lô " + lo.getMaLo() + " còn " + conLai + " ngày là hết hạn"
                    + " (còn tồn " + lo.getSoLuongTon() + ")";
            if (ghiNeuChuaCo(CanhBao.LoaiCanhBao.SAP_HET_HAN, lo, lo.getSanPham(), noiDung)) {
                soMoi++;
            }
        }

        // (4) Lô đã quá hạn nhưng còn tồn
        for (LoHang lo : loHangService.timLoQuaHan(homNay)) {
            String noiDung = "Lô " + lo.getMaLo() + " đã quá hạn ngày "
                    + lo.getHanSuDung() + ", còn tồn " + lo.getSoLuongTon() + " - chỉ được xuất huỷ";
            if (ghiNeuChuaCo(CanhBao.LoaiCanhBao.QUA_HAN, lo, lo.getSanPham(), noiDung)) {
                soMoi++;
            }
        }

        // (5) Sản phẩm có tổng tồn thấp hơn mức tồn tối thiểu
        for (SanPham sp : sanPhamService.laySanPhamDangKinhDoanh()) {
            int tongTon = loHangService.tongTonTheoSanPham(sp.getId());
            if (sp.getTonToiThieu() != null && tongTon < sp.getTonToiThieu()) {
                String noiDung = "Sản phẩm " + sp.getMaSanPham() + " chỉ còn " + tongTon + " "
                        + sp.getDonViTinh() + ", dưới mức tối thiểu " + sp.getTonToiThieu();
                if (ghiNeuChuaCo(CanhBao.LoaiCanhBao.TON_TOI_THIEU, null, sp, noiDung)) {
                    soMoi++;
                }
            }
        }

        return soMoi;
    }

    /** Ghi cảnh báo mới, bỏ qua nếu đã có cảnh báo y hệt đang chờ xử lý. */
    private boolean ghiNeuChuaCo(CanhBao.LoaiCanhBao loai, LoHang lo, SanPham sp, String noiDung) {
        Long daCo = em.createQuery(
                        "SELECT COUNT(c) FROM CanhBao c " +
                        "WHERE c.loaiCanhBao = :loai AND c.daXuLy = 0 " +
                        "AND (:loId IS NULL OR c.loHang.id = :loId) " +
                        "AND (:spId IS NULL OR c.sanPham.id = :spId)", Long.class)
                .setParameter("loai", loai)
                .setParameter("loId", lo != null ? lo.getId() : null)
                .setParameter("spId", sp != null ? sp.getId() : null)
                .getSingleResult();

        if (daCo > 0) {
            return false;
        }

        CanhBao canhBao = new CanhBao();
        canhBao.setLoaiCanhBao(loai);
        canhBao.setLoHang(lo);
        canhBao.setSanPham(sp);
        canhBao.setNoiDung(noiDung);
        canhBao.setNgayCanhBao(LocalDateTime.now());
        canhBao.setDaXuLy(0);
        em.persist(canhBao);
        return true;
    }

    /** Danh sách cảnh báo, lọc theo loại và trạng thái xử lý. Để trống là lấy tất cả. */
    public List<CanhBao> timKiem(CanhBao.LoaiCanhBao loai, Integer daXuLy) {
        // LEFT JOIN FETCH để nạp luôn lô hàng và sản phẩm trong cùng một câu lệnh.
        // Hai quan hệ này khai LAZY, mà open-in-view đang tắt, nên nếu không nạp sẵn
        // thì ra tới template gọi cb.loHang.maLo sẽ văng lỗi.
        StringBuilder jpql = new StringBuilder(
                "SELECT c FROM CanhBao c " +
                "LEFT JOIN FETCH c.loHang " +
                "LEFT JOIN FETCH c.sanPham " +
                "WHERE 1 = 1");
        if (loai != null) {
            jpql.append(" AND c.loaiCanhBao = :loai");
        }
        if (daXuLy != null) {
            jpql.append(" AND c.daXuLy = :daXuLy");
        }
        jpql.append(" ORDER BY c.daXuLy ASC, c.ngayCanhBao DESC");

        var query = em.createQuery(jpql.toString(), CanhBao.class);
        if (loai != null) {
            query.setParameter("loai", loai);
        }
        if (daXuLy != null) {
            query.setParameter("daXuLy", daXuLy);
        }
        return query.getResultList();
    }

    // Đếm cảnh báo chưa xử lý, dùng cho số badge trên dashboard
    public long demChuaXuLy() {
        return em.createQuery("SELECT COUNT(c) FROM CanhBao c WHERE c.daXuLy = 0", Long.class)
                .getSingleResult();
    }

    /**
     * Đánh dấu một cảnh báo là đã xử lý.
     * Chỉ áp dụng cho cảnh báo đang ở trạng thái chưa xử lý (đặc tả mục d).
     */
    @Transactional
    public void danhDauDaXuLy(Long id) {
        CanhBao canhBao = em.find(CanhBao.class, id);
        if (canhBao == null) {
            throw new IllegalArgumentException("Không tìm thấy cảnh báo có id = " + id);
        }
        if (canhBao.getDaXuLy() != null && canhBao.getDaXuLy() == 1) {
            throw new IllegalStateException("Cảnh báo này đã được xử lý trước đó");
        }
        canhBao.setDaXuLy(1);
    }

    public List<CanhBao> layDanhSachRutGon(int soLuong) {
        List<CanhBao> tatCa = timKiem(null, 0);
        return new ArrayList<>(tatCa.subList(0, Math.min(soLuong, tatCa.size())));
    }
}
