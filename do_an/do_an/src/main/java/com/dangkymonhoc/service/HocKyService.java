package com.dangkymonhoc.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.dto.HocKyForm;
import com.dangkymonhoc.dto.HocKyRow;
import com.dangkymonhoc.entity.HocKy;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.HocKyRepository;
import com.dangkymonhoc.repository.LichHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;

import lombok.RequiredArgsConstructor;

/**
 * Học kỳ: mẫu mã HK{1|2|3}-{năm}, thời gian học (bảng HOC_KY), học kỳ hiện tại, sắp xếp mới nhất lên trước
 * và quản lý danh mục học kỳ (MH23).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HocKyService {

    public static final String DA_QUA = "DA_QUA";
    public static final String HIEN_TAI = "HIEN_TAI";
    public static final String SAP_TOI = "SAP_TOI";

    private static final Pattern MAU_HOC_KY = Pattern.compile("^HK([123])-(\\d{4})$");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** HK1-2026 > HK3-2025 > HK2-2025 > HK1-2025; chuỗi sai mẫu xếp cuối. */
    private static final Comparator<String> MOI_NHAT_TRUOC = Comparator
            .comparingInt(HocKyService::thuTu).reversed()
            .thenComparing(Comparator.naturalOrder());

    private final HocKyRepository hocKyRepository;
    private final PhanCongRepository phanCongRepository;
    private final LichHocRepository lichHocRepository;
    private final AppProperties appProperties;

    // ===================== Học kỳ hiện tại =====================

    /**
     * Học kỳ hiện tại: lấy app.hoc-ky-hien-tai nếu có cấu hình, ngược lại tính theo ngày hôm nay
     * (xem {@link #theoNgay}).
     */
    public String hienTai() {
        String cauHinh = appProperties.getHocKyHienTai();
        if (cauHinh != null && !cauHinh.isBlank()) {
            return cauHinh.trim().toUpperCase();
        }
        return theoNgay(LocalDate.now());
    }

    /**
     * Học kỳ đang diễn ra vào {@code ngay}; đang nghỉ giữa hai học kỳ thì lấy học kỳ sắp tới (để sinh viên đăng ký
     * trước); sau học kỳ cuối cùng thì lấy học kỳ cuối cùng. Chưa khai báo học kỳ nào thì đoán theo tháng.
     */
    String theoNgay(LocalDate ngay) {
        List<HocKy> ds = hocKyRepository.findAllTheoNgay();
        for (HocKy hk : ds) {
            if (!ngay.isAfter(hk.getNgayKetThuc())) {
                return hk.getMaHK();
            }
        }
        if (!ds.isEmpty()) {
            return ds.get(ds.size() - 1).getMaHK();
        }
        int nam = ngay.getYear();
        int thang = ngay.getMonthValue();
        if (thang >= 8) {
            return "HK1-" + nam;
        }
        if (thang == 1) {
            return "HK1-" + (nam - 1);
        }
        return (thang <= 5 ? "HK2-" : "HK3-") + (nam - 1);
    }

    /**
     * Thời gian mặc định, dùng khi nạp dữ liệu mẫu và khi CSDL cũ có học kỳ chưa khai báo ngày.
     * Năm học Y–(Y+1): HK1 từ thứ 2 đầu tiên của tháng 9/Y, 18 tuần; HK2 từ thứ 2 đầu tiên của tháng 2/(Y+1),
     * 18 tuần; HK3 (hè) từ thứ 2 đầu tiên sau ngày 15/6/(Y+1), 8 tuần.
     */
    public HocKy macDinh(String maHK) {
        Matcher m = MAU_HOC_KY.matcher(maHK);
        if (!m.matches()) {
            throw new IllegalArgumentException("Học kỳ sai mẫu: " + maHK);
        }
        int ky = Integer.parseInt(m.group(1));
        int nam = Integer.parseInt(m.group(2));
        LocalDate moc = ky == 1 ? LocalDate.of(nam, 9, 1) : ky == 2 ? LocalDate.of(nam + 1, 2, 1)
                : LocalDate.of(nam + 1, 6, 15);
        LocalDate batDau = moc.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        int soTuan = ky == 3 ? 8 : 18;
        return new HocKy(maHK, batDau, batDau.plusWeeks(soTuan).minusDays(1));
    }

    // ===================== Tra cứu =====================

    public Optional<HocKy> tim(String maHK) {
        return maHK == null ? Optional.empty() : hocKyRepository.findById(maHK.trim().toUpperCase());
    }

    public HocKy lay(String maHK) {
        return tim(maHK).orElseThrow(() -> new BusinessException("Không tìm thấy học kỳ " + maHK + "."));
    }

    /** Đúng mẫu HK{1|2|3}-{năm}. */
    public boolean dungMau(String hocKy) {
        return hocKy != null && MAU_HOC_KY.matcher(hocKy).matches();
    }

    /** Đã khai báo trong danh mục học kỳ. */
    public boolean tonTai(String hocKy) {
        return dungMau(hocKy) && hocKyRepository.existsById(hocKy);
    }

    /** Học kỳ đứng trước học kỳ hiện tại, ví dụ HK2-2025 khi hiện tại là HK1-2026. */
    public boolean daQua(String hocKy) {
        return dungMau(hocKy) && thuTu(hocKy) < thuTu(hienTai());
    }

    /** DA_QUA, HIEN_TAI hoặc SAP_TOI so với học kỳ hiện tại. */
    public String trangThai(String hocKy) {
        int soSanh = Integer.compare(thuTu(hocKy), thuTu(hienTai()));
        return soSanh < 0 ? DA_QUA : soSanh == 0 ? HIEN_TAI : SAP_TOI;
    }

    /** Trả về hocKy nếu đã khai báo trong danh mục, ngược lại trả về học kỳ hiện tại. */
    public String chuanHoa(String hocKy) {
        String hk = hocKy == null ? "" : hocKy.trim().toUpperCase();
        return tonTai(hk) ? hk : hienTai();
    }

    /** Các học kỳ đã khai báo cộng thêm học kỳ hiện tại và các học kỳ truyền vào, mới nhất trước. */
    public List<String> tatCa(String... them) {
        TreeSet<String> ds = new TreeSet<>(MOI_NHAT_TRUOC);
        for (HocKy hk : hocKyRepository.findAll()) {
            ds.add(hk.getMaHK());
        }
        ds.add(hienTai());
        for (String hk : them) {
            if (dungMau(hk)) {
                ds.add(hk);
            }
        }
        return List.copyOf(ds);
    }

    /** Sắp xếp một tập học kỳ bất kỳ, luôn có học kỳ hiện tại. */
    public List<String> sapXep(Collection<String> hocKys) {
        TreeSet<String> ds = new TreeSet<>(MOI_NHAT_TRUOC);
        ds.addAll(hocKys);
        ds.add(hienTai());
        return List.copyOf(ds);
    }

    public static Comparator<String> moiNhatTruoc() {
        return MOI_NHAT_TRUOC;
    }

    // ===================== MH23 Quản lý học kỳ =====================

    /** Mọi học kỳ, mới nhất trước, kèm trạng thái và số phân công. */
    public List<HocKyRow> danhSach() {
        Map<String, Long> soPhanCong = new HashMap<>();
        for (Object[] row : phanCongRepository.countTheoHocKy()) {
            soPhanCong.put((String) row[0], (Long) row[1]);
        }
        List<HocKyRow> ds = new ArrayList<>();
        for (HocKy hk : hocKyRepository.findAll()) {
            ds.add(new HocKyRow(hk, trangThai(hk.getMaHK()), soPhanCong.getOrDefault(hk.getMaHK(), 0L)));
        }
        ds.sort(Comparator.comparing((HocKyRow r) -> r.hocKy().getMaHK(), MOI_NHAT_TRUOC));
        return ds;
    }

    /**
     * Thêm hoặc sửa thời gian học kỳ. Ngày bắt đầu là thứ 2 (đầu tuần 1), các học kỳ không trùng thời gian, và
     * không được rút ngắn học kỳ khi lịch học đã xếp vượt quá số tuần mới.
     *
     * @return mã học kỳ để hiển thị thông báo
     */
    @Transactional
    public String luu(HocKyForm form) {
        String ma = form.getMaHK();
        LocalDate batDau = form.getNgayBatDau();
        LocalDate ketThuc = form.getNgayKetThuc();
        if (batDau.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new BusinessException("Ngày bắt đầu phải là thứ 2 (ngày đầu tiên của tuần 1).");
        }
        if (!ketThuc.isAfter(batDau)) {
            throw new BusinessException("Ngày kết thúc phải sau ngày bắt đầu.");
        }
        for (HocKy khac : hocKyRepository.findAll()) {
            if (!khac.getMaHK().equals(ma) && !batDau.isAfter(khac.getNgayKetThuc())
                    && !khac.getNgayBatDau().isAfter(ketThuc)) {
                throw new BusinessException("Thời gian trùng với học kỳ " + khac.getMaHK() + " ("
                        + khac.getNgayBatDau().format(NGAY) + " – " + khac.getNgayKetThuc().format(NGAY) + ").");
            }
        }

        HocKy hk;
        if (form.isEditMode()) {
            hk = lay(ma);
            int soTuanMoi = new HocKy(ma, batDau, ketThuc).getSoTuan();
            Integer tuanCuoi = lichHocRepository.maxTuanKetThuc(ma);
            if (tuanCuoi != null && tuanCuoi > soTuanMoi) {
                throw new BusinessException("Học kỳ mới chỉ có " + soTuanMoi + " tuần nhưng lịch học đã xếp đến tuần "
                        + tuanCuoi + ". Hãy sửa lịch học trước.");
            }
        } else {
            if (hocKyRepository.existsById(ma)) {
                throw new BusinessException("Học kỳ " + ma + " đã tồn tại.");
            }
            hk = new HocKy();
            hk.setMaHK(ma);
        }
        hk.setNgayBatDau(batDau);
        hk.setNgayKetThuc(ketThuc);
        hocKyRepository.save(hk);
        return ma;
    }

    @Transactional
    public void xoa(String maHK) {
        HocKy hk = lay(maHK);
        long soPhanCong = phanCongRepository.countByHocKy(hk.getMaHK());
        if (soPhanCong > 0) {
            throw new BusinessException("Không thể xóa: học kỳ " + hk.getMaHK() + " đang có " + soPhanCong
                    + " phân công giảng dạy.");
        }
        hocKyRepository.delete(hk);
    }

    private static int thuTu(String hocKy) {
        Matcher m = MAU_HOC_KY.matcher(hocKy == null ? "" : hocKy);
        if (!m.matches()) {
            return Integer.MIN_VALUE;
        }
        return Integer.parseInt(m.group(2)) * 10 + Integer.parseInt(m.group(1));
    }
}
