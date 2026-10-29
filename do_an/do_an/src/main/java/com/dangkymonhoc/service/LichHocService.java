package com.dangkymonhoc.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.common.TietHoc;
import com.dangkymonhoc.dto.BangXepLich;
import com.dangkymonhoc.dto.BuoiHocRow;
import com.dangkymonhoc.dto.ChuaXepRow;
import com.dangkymonhoc.dto.KetQuaXepLich;
import com.dangkymonhoc.dto.LichHocForm;
import com.dangkymonhoc.entity.GiangVien;
import com.dangkymonhoc.entity.HocKy;
import com.dangkymonhoc.entity.LichHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.PhanCongId;
import com.dangkymonhoc.entity.Phong;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.LichHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;
import com.dangkymonhoc.repository.PhongRepository;

import lombok.RequiredArgsConstructor;

/**
 * MH20 Xếp lịch dạy. Các ràng buộc ở đây dùng chung cho xếp tự động và sửa tay:
 * <ul>
 * <li>buổi học nằm trọn trong buổi sáng (tiết 1–5) hoặc chiều (tiết 6–10);</li>
 * <li>khoảng tuần học nằm trong số tuần của học kỳ;</li>
 * <li>một giảng viên không có hai buổi giao nhau;</li>
 * <li>một phòng không có hai buổi giao nhau;</li>
 * <li>sức chứa phòng ≥ số sinh viên đã đăng ký lớp;</li>
 * <li>sinh viên đã đăng ký hai lớp thì hai lớp đó không giao nhau.</li>
 * </ul>
 * Hai buổi giao nhau khi cùng thứ, khoảng tiết chồng lên nhau và có tuần học chung. Mỗi lớp học đủ
 * {@link TietHoc#soTuanCan} tuần (15 tiết mỗi tín chỉ), mặc định từ tuần 1.
 * <p>
 * Học kỳ đã qua chỉ được xem lịch và xóa bản nháp; xếp, sửa, lưu chính thức chỉ cho học kỳ hiện tại trở về sau.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LichHocService {

    public static final int THU_DAU = 2;
    public static final int THU_CUOI = 7;
    private static final int SO_MAU = 8;

    private final LichHocRepository lichHocRepository;
    private final PhanCongRepository phanCongRepository;
    private final PhongRepository phongRepository;
    private final DangKyRepository dangKyRepository;
    private final HocKyService hocKyService;

    // ===================== Học kỳ được xếp =====================

    /** Học kỳ hiện tại hoặc tương lai; học kỳ đã qua chỉ được xem. */
    public boolean choPhepXep(String hocKy) {
        return !hocKyService.daQua(hocKy);
    }

    private void kiemTraChoPhepXep(String hocKy) {
        if (!choPhepXep(hocKy)) {
            throw new BusinessException("Học kỳ " + hocKy + " đã qua, chỉ được xem lịch. Chỉ xếp lịch cho học kỳ "
                    + "hiện tại (" + hocKyService.hienTai() + ") trở về sau.");
        }
    }

    // ===================== Quy ước tiết =====================

    /** Sáng tiết 1–5, chiều tiết 6–10; một buổi học không được vắt qua hai buổi. */
    public static boolean tietHopLe(int tietBatDau, int soTiet) {
        if (soTiet < 1) {
            return false;
        }
        int tietKetThuc = tietBatDau + soTiet - 1;
        boolean buoiSang = tietBatDau >= 1 && tietKetThuc <= TietHoc.SO_TIET_MOT_BUOI;
        boolean buoiChieu = tietBatDau >= TietHoc.SO_TIET_MOT_BUOI + 1 && tietKetThuc <= TietHoc.SO_TIET_MOT_NGAY;
        return buoiSang || buoiChieu;
    }

    /** Các tiết bắt đầu hợp lệ của một buổi có {@code soTiet} tiết, tăng dần. */
    public static List<Integer> tietBatDauHopLe(int soTiet) {
        List<Integer> ds = new ArrayList<>();
        for (int tiet = 1; tiet <= TietHoc.SO_TIET_MOT_NGAY; tiet++) {
            if (tietHopLe(tiet, soTiet)) {
                ds.add(tiet);
            }
        }
        return ds;
    }

    /** Một buổi/tuần, số tiết bằng số tín chỉ nên số tuần cần học là 15 với mọi môn. */
    private static int soTuanCan(PhanCong pc) {
        int soTinChi = pc.getMonHoc().getSoTinChi();
        return TietHoc.soTuanCan(soTinChi, soTinChi);
    }

    // ===================== Ràng buộc =====================

    /**
     * Kiểm tra một buổi với các buổi khác trong học kỳ.
     *
     * @param cacBuoi   các buổi của học kỳ; buổi cùng phân công với {@code buoi} được bỏ qua
     * @param svTheoLop sinh viên đã đăng ký từng lớp
     * @param soTuan    số tuần của học kỳ
     * @return lý do vi phạm đầu tiên, null nếu hợp lệ
     */
    String kiemTra(LichHoc buoi, List<LichHoc> cacBuoi, Map<PhanCongId, Set<String>> svTheoLop, int soTuan) {
        String loi = saiThoiGian(buoi, soTuan);
        if (loi == null) {
            loi = trungGiangVien(buoi, cacBuoi);
        }
        if (loi == null) {
            loi = trungPhong(buoi, cacBuoi);
        }
        if (loi == null) {
            loi = thieuSucChua(buoi, svTheoLop);
        }
        if (loi == null) {
            loi = trungSinhVien(buoi, cacBuoi, svTheoLop);
        }
        return loi;
    }

    private String saiThoiGian(LichHoc buoi, int soTuan) {
        if (buoi.getThu() < THU_DAU || buoi.getThu() > THU_CUOI) {
            return "Thứ phải từ 2 đến 7.";
        }
        if (!tietHopLe(buoi.getTietBatDau(), buoi.getSoTiet())) {
            return "Buổi " + buoi.getSoTiet() + " tiết bắt đầu tiết " + buoi.getTietBatDau()
                    + " bị vắt qua hai buổi. Buổi sáng tiết 1–5, buổi chiều tiết 6–10.";
        }
        if (buoi.getTuanBatDau() < 1 || buoi.getTuanKetThuc() > soTuan) {
            return "Lớp học tuần " + buoi.getTuanBatDau() + "–" + buoi.getTuanKetThuc() + " nhưng học kỳ chỉ có "
                    + soTuan + " tuần.";
        }
        return null;
    }

    private String trungGiangVien(LichHoc buoi, List<LichHoc> cacBuoi) {
        String maGV = maGV(buoi);
        for (LichHoc khac : cacBuoi) {
            if (!cungLop(buoi, khac) && maGV.equals(maGV(khac)) && buoi.giaoNhau(khac)) {
                return maGV + " đã dạy " + maMH(khac) + " " + thuTiet(khac) + ".";
            }
        }
        return null;
    }

    private String trungPhong(LichHoc buoi, List<LichHoc> cacBuoi) {
        String maPhong = buoi.getPhong().getMaPhong();
        for (LichHoc khac : cacBuoi) {
            if (!cungLop(buoi, khac) && maPhong.equals(khac.getPhong().getMaPhong()) && buoi.giaoNhau(khac)) {
                return "Phòng " + maPhong + " đã có lớp " + maMH(khac) + " (" + maGV(khac) + ") " + thuTiet(khac) + ".";
            }
        }
        return null;
    }

    private String thieuSucChua(LichHoc buoi, Map<PhanCongId, Set<String>> svTheoLop) {
        int siSo = siSo(buoi.getPhanCong().getId(), svTheoLop);
        Phong phong = buoi.getPhong();
        if (phong.getSucChua() < siSo) {
            return "Phòng " + phong.getMaPhong() + " chỉ có " + phong.getSucChua() + " chỗ, lớp "
                    + maMH(buoi) + " (" + maGV(buoi) + ") có " + siSo + " sinh viên.";
        }
        return null;
    }

    private String trungSinhVien(LichHoc buoi, List<LichHoc> cacBuoi, Map<PhanCongId, Set<String>> svTheoLop) {
        Set<String> svLopNay = svTheoLop.getOrDefault(buoi.getPhanCong().getId(), Set.of());
        if (svLopNay.isEmpty()) {
            return null;
        }
        for (LichHoc khac : cacBuoi) {
            if (cungLop(buoi, khac) || !buoi.giaoNhau(khac)) {
                continue;
            }
            for (String maSV : svTheoLop.getOrDefault(khac.getPhanCong().getId(), Set.of())) {
                if (svLopNay.contains(maSV)) {
                    return "Sinh viên " + maSV + " đã học " + maMH(khac) + " " + thuTiet(khac) + ".";
                }
            }
        }
        return null;
    }

    // ===================== Tự động xếp =====================

    /**
     * Thuật toán tham lam, không ngẫu nhiên nên chạy lại ra cùng kết quả:
     * <ol>
     * <li>Xóa các buổi nháp chưa ghim; buổi đã ghim và buổi chính thức giữ nguyên.</li>
     * <li>Sắp phân công chưa có lịch: số tiết giảm dần, giảng viên nhiều phân công hơn xếp trước.</li>
     * <li>Mỗi phân công học từ tuần 1, lấy vị trí đầu tiên không vi phạm ràng buộc (xem {@link #timViTri}).</li>
     * </ol>
     */
    @Transactional
    public KetQuaXepLich tuDongXep(String hocKy) {
        kiemTraChoPhepXep(hocKy);
        HocKy hk = hocKyService.lay(hocKy);
        List<LichHoc> daXep = new ArrayList<>(lichHocRepository.findByHocKy(hocKy));
        List<LichHoc> xoa = new ArrayList<>();
        for (LichHoc l : daXep) {
            if (!l.isChinhThuc() && !l.isDaGhim()) {
                xoa.add(l);
            }
        }
        lichHocRepository.deleteAll(xoa);
        // Xóa ngay để không vướng ràng buộc UNIQUE khi thêm lại buổi của cùng phân công
        lichHocRepository.flush();
        daXep.removeAll(xoa);

        Map<PhanCongId, Set<String>> svTheoLop = sinhVienTheoLop(hocKy);
        List<Phong> phongs = phongRepository.findAllSorted();
        int soXep = 0;
        List<ChuaXepRow> chuaXep = new ArrayList<>();
        for (PhanCong pc : phanCongChuaXep(hocKy, daXep)) {
            LichHoc buoi = timViTri(pc, daXep, phongs, svTheoLop, hk);
            if (buoi == null) {
                chuaXep.add(chuaXepRow(pc, lyDoKhongXep(pc, daXep, phongs, svTheoLop, hk), svTheoLop));
            } else {
                daXep.add(lichHocRepository.save(buoi));
                soXep++;
            }
        }
        return new KetQuaXepLich(soXep, chuaXep);
    }

    /** Phân công chưa có lịch, theo thứ tự xếp: số tiết giảm dần → GV nhiều phân công trước → mã GV → mã môn. */
    private List<PhanCong> phanCongChuaXep(String hocKy, List<LichHoc> daXep) {
        Set<PhanCongId> coLich = new HashSet<>();
        for (LichHoc l : daXep) {
            coLich.add(l.getPhanCong().getId());
        }
        List<PhanCong> tatCa = phanCongRepository.findByHocKy(hocKy);
        Map<String, Integer> soPhanCong = new HashMap<>();
        for (PhanCong pc : tatCa) {
            soPhanCong.merge(pc.getId().getMaGV(), 1, Integer::sum);
        }

        List<PhanCong> ds = new ArrayList<>();
        for (PhanCong pc : tatCa) {
            if (!coLich.contains(pc.getId())) {
                ds.add(pc);
            }
        }
        ds.sort(Comparator.comparingInt((PhanCong pc) -> -pc.getMonHoc().getSoTinChi())
                .thenComparingInt(pc -> -soPhanCong.get(pc.getId().getMaGV()))
                .thenComparing(pc -> pc.getId().getMaGV())
                .thenComparing(pc -> pc.getId().getMaMH()));
        return ds;
    }

    /**
     * Lớp học từ tuần 1 đến tuần thứ {@link #soTuanCan}. Duyệt thứ (xem {@link #thuUuTien}) → tiết bắt đầu hợp lệ
     * tăng dần → phòng nhỏ nhất đủ sức chứa; trả về vị trí đầu tiên không vi phạm ràng buộc, null nếu không có.
     */
    private LichHoc timViTri(PhanCong pc, List<LichHoc> daXep, List<Phong> phongs,
                             Map<PhanCongId, Set<String>> svTheoLop, HocKy hk) {
        int soTiet = pc.getMonHoc().getSoTinChi();
        int soTuan = soTuanCan(pc);
        if (soTuan > hk.getSoTuan()) {
            return null;
        }
        for (int thu : thuUuTien(pc, daXep, svTheoLop)) {
            for (int tiet : tietBatDauHopLe(soTiet)) {
                for (Phong phong : phongs) {
                    LichHoc buoi = new LichHoc(pc, thu, tiet, soTiet, 1, soTuan, phong);
                    if (kiemTra(buoi, daXep, svTheoLop, hk.getSoTuan()) == null) {
                        return buoi;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Thứ 2–7 sắp tăng dần theo số buổi đã có trong ngày của giảng viên hoặc của sinh viên lớp này (buổi của lớp
     * có chung sinh viên), để rải đều lịch cho cả giảng viên lẫn sinh viên thay vì dồn vào thứ 2.
     */
    private List<Integer> thuUuTien(PhanCong pc, List<LichHoc> daXep, Map<PhanCongId, Set<String>> svTheoLop) {
        String maGV = pc.getId().getMaGV();
        Set<String> svLopNay = svTheoLop.getOrDefault(pc.getId(), Set.of());
        Map<Integer, Integer> soBuoi = new HashMap<>();
        for (LichHoc l : daXep) {
            boolean cungGiangVien = maGV.equals(maGV(l));
            boolean chungSinhVien = !Collections.disjoint(svLopNay,
                    svTheoLop.getOrDefault(l.getPhanCong().getId(), Set.of()));
            if (cungGiangVien || chungSinhVien) {
                soBuoi.merge(l.getThu(), 1, Integer::sum);
            }
        }
        List<Integer> thus = new ArrayList<>();
        for (int thu = THU_DAU; thu <= THU_CUOI; thu++) {
            thus.add(thu);
        }
        // List.sort giữ nguyên thứ tự các phần tử bằng nhau nên thứ nhỏ vẫn đứng trước
        thus.sort(Comparator.comparingInt(thu -> soBuoi.getOrDefault(thu, 0)));
        return thus;
    }

    /** Giải thích vì sao {@link #timViTri} không tìm được chỗ cho phân công. */
    private String lyDoKhongXep(PhanCong pc, List<LichHoc> daXep, List<Phong> phongs,
                                Map<PhanCongId, Set<String>> svTheoLop, HocKy hk) {
        int soTiet = pc.getMonHoc().getSoTinChi();
        if (tietBatDauHopLe(soTiet).isEmpty()) {
            return "Môn có " + soTiet + " tiết, dài hơn một buổi học (tối đa " + TietHoc.SO_TIET_MOT_BUOI + " tiết).";
        }
        int soTuan = soTuanCan(pc);
        if (soTuan > hk.getSoTuan()) {
            return "Môn cần học " + soTuan + " tuần nhưng học kỳ chỉ có " + hk.getSoTuan() + " tuần.";
        }
        int siSo = siSo(pc.getId(), svTheoLop);
        if (phongs.stream().noneMatch(p -> p.getSucChua() >= siSo)) {
            return "Không có phòng đủ chỗ cho " + siSo + " sinh viên.";
        }
        boolean gvCoGioTrong = false;
        boolean svCoGioTrong = false;
        for (int thu = THU_DAU; thu <= THU_CUOI; thu++) {
            for (int tiet : tietBatDauHopLe(soTiet)) {
                LichHoc thuNghiem = new LichHoc(pc, thu, tiet, soTiet, 1, soTuan, phongs.get(0));
                if (trungGiangVien(thuNghiem, daXep) == null) {
                    gvCoGioTrong = true;
                    if (trungSinhVien(thuNghiem, daXep, svTheoLop) == null) {
                        svCoGioTrong = true;
                    }
                }
            }
        }
        if (!gvCoGioTrong) {
            return "Giảng viên " + pc.getId().getMaGV() + " không còn khung " + soTiet + " tiết trống trong tuần.";
        }
        if (!svCoGioTrong) {
            return "Sinh viên của lớp bị trùng lịch ở mọi khung giờ giảng viên còn trống.";
        }
        return "Không còn phòng trống đủ chỗ cho " + siSo + " sinh viên vào các khung giờ còn trống.";
    }

    // ===================== Màn hình MH20 =====================

    /** Lưới và danh sách buổi học của học kỳ, xung đột, phân công chưa xếp được và trạng thái. */
    public BangXepLich bangXepLich(String hocKy) {
        HocKy hk = hocKyService.lay(hocKy);
        List<LichHoc> ds = lichHocRepository.findByHocKy(hocKy);
        Map<PhanCongId, Set<String>> svTheoLop = sinhVienTheoLop(hocKy);
        List<Phong> phongs = phongRepository.findAllSorted();
        Map<String, Integer> mau = mauTheoGiangVien(hocKy);

        List<BuoiHocRow> rows = new ArrayList<>();
        int soNhap = 0;
        int soXungDot = 0;
        for (LichHoc l : ds) {
            String xungDot = kiemTra(l, ds, svTheoLop, hk.getSoTuan());
            if (xungDot != null) {
                soXungDot++;
            }
            if (!l.isChinhThuc()) {
                soNhap++;
            }
            rows.add(toRow(l, hk, xungDot, mau.getOrDefault(maGV(l), 0), svTheoLop));
        }

        boolean choPhepXep = choPhepXep(hocKy);
        List<ChuaXepRow> chuaXep = new ArrayList<>();
        for (PhanCong pc : phanCongChuaXep(hocKy, ds)) {
            String lyDo = !choPhepXep ? "Học kỳ đã qua, không còn xếp lịch được."
                    : timViTri(pc, ds, phongs, svTheoLop, hk) != null
                    ? "Chưa xếp lịch. Bấm \"Tự động xếp lịch\" hoặc \"Xếp tay\"."
                    : lyDoKhongXep(pc, ds, phongs, svTheoLop, hk);
            chuaXep.add(chuaXepRow(pc, lyDo, svTheoLop));
        }

        String trangThai = ds.isEmpty() ? BangXepLich.TRONG : soNhap > 0 ? LichHoc.NHAP : LichHoc.CHINH_THUC;
        int soPhanCong = ds.size() + chuaXep.size();
        return new BangXepLich(rows, chuaXep, trangThai, soPhanCong, soNhap, soXungDot);
    }

    /** Lọc theo giảng viên ("GV:GV01") hoặc phòng ("P:A101"); chuỗi rỗng là tất cả. */
    public List<BuoiHocRow> loc(List<BuoiHocRow> ds, String loc) {
        if (loc == null || loc.isBlank()) {
            return ds;
        }
        List<BuoiHocRow> kq = new ArrayList<>();
        for (BuoiHocRow b : ds) {
            if (loc.equals("GV:" + b.getMaGV()) || loc.equals("P:" + b.getMaPhong())) {
                kq.add(b);
            }
        }
        return kq;
    }

    public List<Phong> dsPhong() {
        return phongRepository.findAllSorted();
    }

    /** Giảng viên có phân công trong học kỳ (bộ lọc "Xem theo" và chú thích màu). */
    public List<GiangVien> giangVienTrongHocKy(String hocKy) {
        Map<String, GiangVien> ds = new TreeMap<>();
        for (PhanCong pc : phanCongRepository.findByHocKy(hocKy)) {
            ds.put(pc.getGiangVien().getMaGV(), pc.getGiangVien());
        }
        return new ArrayList<>(ds.values());
    }

    /** Màu thẻ theo giảng viên: thứ tự mã GV trong học kỳ, quay vòng 8 màu. */
    public Map<String, Integer> mauTheoGiangVien(String hocKy) {
        Map<String, Integer> mau = new HashMap<>();
        int i = 0;
        for (GiangVien gv : giangVienTrongHocKy(hocKy)) {
            mau.put(gv.getMaGV(), i++ % SO_MAU);
        }
        return mau;
    }

    /** Có ít nhất một buổi chính thức: sinh viên đã thấy lịch và được đăng ký. */
    public boolean daCongBo(String hocKy) {
        return lichHocRepository.countChinhThuc(hocKy) > 0;
    }

    @Transactional
    public int xoaBanNhap(String hocKy) {
        List<LichHoc> nhap = new ArrayList<>();
        for (LichHoc l : lichHocRepository.findByHocKy(hocKy)) {
            if (!l.isChinhThuc()) {
                nhap.add(l);
            }
        }
        lichHocRepository.deleteAll(nhap);
        return nhap.size();
    }

    /** Chuyển mọi buổi của học kỳ sang CHINH_THUC; chỉ khi không còn xung đột và mọi phân công đã có lịch. */
    @Transactional
    public int luuChinhThuc(String hocKy) {
        kiemTraChoPhepXep(hocKy);
        BangXepLich bang = bangXepLich(hocKy);
        if (!bang.chuaXep().isEmpty()) {
            throw new BusinessException("Còn " + bang.chuaXep().size()
                    + " phân công chưa có lịch, chưa thể lưu chính thức.");
        }
        if (bang.soXungDot() > 0) {
            throw new BusinessException("Còn " + bang.soXungDot()
                    + " buổi bị xung đột, hãy sửa trước khi lưu chính thức.");
        }
        if (bang.soNhap() == 0) {
            throw new BusinessException(bang.buoi().isEmpty()
                    ? "Học kỳ " + hocKy + " chưa có lịch để lưu."
                    : "Lịch học kỳ " + hocKy + " đã là chính thức.");
        }
        int dem = 0;
        for (LichHoc l : lichHocRepository.findByHocKy(hocKy)) {
            if (!l.isChinhThuc()) {
                l.setTrangThai(LichHoc.CHINH_THUC);
                dem++;
            }
        }
        return dem;
    }

    /**
     * Sửa một buổi (maLich có giá trị) hoặc xếp tay phân công chưa có lịch. Đổi thứ, tiết, tuần hoặc phòng thì buổi
     * tự được ghim. Buổi đã chính thức phải có xác nhận vì sinh viên đã thấy lịch.
     *
     * @return thông báo thành công
     */
    @Transactional
    public String luuBuoi(LichHocForm form) {
        String hocKy = form.getHocKy();
        kiemTraChoPhepXep(hocKy);
        HocKy hk = hocKyService.lay(hocKy);
        PhanCong pc = phanCongRepository.findById(new PhanCongId(form.getMaGV(), form.getMaMH(), hocKy))
                .orElseThrow(() -> new BusinessException("Không tìm thấy phân công cần xếp lịch."));
        Phong phong = phongRepository.findById(form.getMaPhong())
                .orElseThrow(() -> new BusinessException("Vui lòng chọn phòng học."));
        Optional<LichHoc> hienCo = lichHocRepository.findByPhanCong(form.getMaGV(), form.getMaMH(), hocKy);

        LichHoc buoi;
        if (form.getMaLich() == null) {
            if (hienCo.isPresent()) {
                throw new BusinessException("Phân công này đã có lịch, hãy sửa buổi hiện có.");
            }
            buoi = null;
        } else {
            buoi = hienCo.filter(l -> l.getMaLich().equals(form.getMaLich()))
                    .orElseThrow(() -> new BusinessException("Không tìm thấy buổi học cần sửa."));
            if (buoi.isChinhThuc() && !form.isXacNhan()) {
                throw new BusinessException("Lịch học kỳ đã chính thức, sinh viên đã thấy lịch này. "
                        + "Hãy đánh dấu ô xác nhận trước khi lưu.");
            }
        }

        // Kiểm tra trên một bản thử để không đổi dữ liệu khi vi phạm
        int soTiet = pc.getMonHoc().getSoTinChi();
        int tuanBatDau = form.getTuanBatDau() == null ? 1 : form.getTuanBatDau();
        int tuanKetThuc = tuanBatDau + soTuanCan(pc) - 1;
        LichHoc thuNghiem = new LichHoc(pc, form.getThu(), form.getTietBatDau(), soTiet, tuanBatDau, tuanKetThuc,
                phong);
        String loi = kiemTra(thuNghiem, lichHocRepository.findByHocKy(hocKy), sinhVienTheoLop(hocKy),
                hk.getSoTuan());
        if (loi != null) {
            throw new BusinessException(loi);
        }

        if (buoi == null) {
            thuNghiem.setGhim(true);
            buoi = lichHocRepository.save(thuNghiem);
        } else {
            boolean doiViTri = !buoi.getThu().equals(form.getThu())
                    || !buoi.getTietBatDau().equals(form.getTietBatDau())
                    || buoi.getTuanBatDau() != tuanBatDau
                    || !buoi.getPhong().getMaPhong().equals(phong.getMaPhong());
            buoi.setThu(form.getThu());
            buoi.setTietBatDau(form.getTietBatDau());
            buoi.setSoTiet(soTiet);
            buoi.setTuanBatDau(tuanBatDau);
            buoi.setTuanKetThuc(tuanKetThuc);
            buoi.setPhong(phong);
            buoi.setGhim(doiViTri || form.isGhim());
        }
        return "Đã lưu lịch " + pc.getMonHoc().getMaMH() + " – " + pc.getGiangVien().getHoTen() + ": "
                + buoi.getMoTa() + (buoi.isDaGhim() ? " (đã ghim)." : ".");
    }

    // ===================== MH21, MH22 =====================

    /** Lịch học chính thức của sinh viên, màu thẻ theo môn. */
    public List<BuoiHocRow> lichCuaSinhVien(String maSV, String hocKy) {
        return toRowsChiDoc(lichHocRepository.findChinhThucCuaSinhVien(maSV, hocKy), hocKy);
    }

    /** Lịch dạy chính thức của giảng viên, màu thẻ theo môn. */
    public List<BuoiHocRow> lichCuaGiangVien(String maGV, String hocKy) {
        return toRowsChiDoc(lichHocRepository.findChinhThucCuaGiangVien(maGV, hocKy), hocKy);
    }

    /** Các buổi có học trong tuần {@code tuan}; tuần 0 nghĩa là cả học kỳ. */
    public static List<BuoiHocRow> locTheoTuan(List<BuoiHocRow> ds, int tuan) {
        if (tuan == 0) {
            return ds;
        }
        List<BuoiHocRow> kq = new ArrayList<>();
        for (BuoiHocRow b : ds) {
            if (b.getTuanBatDau() <= tuan && tuan <= b.getTuanKetThuc()) {
                kq.add(b);
            }
        }
        return kq;
    }

    /** Tuần mặc định khi mở lịch: tuần chứa hôm nay, trước học kỳ thì tuần 1, sau học kỳ thì tuần cuối. */
    public static int tuanMacDinh(HocKy hk) {
        return Math.max(1, Math.min(hk.tuanCua(LocalDate.now()), hk.getSoTuan()));
    }

    /** Thứ 2–7 → ngày cụ thể trong tuần {@code tuan} của học kỳ, để ghi lên đầu cột lưới tuần. */
    public static Map<Integer, LocalDate> ngayTrongTuan(HocKy hk, int tuan) {
        Map<Integer, LocalDate> ngay = new TreeMap<>();
        for (int thu = THU_DAU; thu <= THU_CUOI; thu++) {
            ngay.put(thu, hk.ngayHoc(tuan, thu));
        }
        return ngay;
    }

    private List<BuoiHocRow> toRowsChiDoc(List<LichHoc> ds, String hocKy) {
        HocKy hk = hocKyService.tim(hocKy).orElse(null);
        Map<String, Integer> mau = mauTheo(ds, LichHocService::maMH);
        List<BuoiHocRow> rows = new ArrayList<>();
        for (LichHoc l : ds) {
            rows.add(toRow(l, hk, null, mau.get(maMH(l)), Map.of()));
        }
        return rows;
    }

    private static Map<String, Integer> mauTheo(List<LichHoc> ds, Function<LichHoc, String> khoa) {
        Set<String> cacKhoa = new TreeSet<>();
        for (LichHoc l : ds) {
            cacKhoa.add(khoa.apply(l));
        }
        Map<String, Integer> mau = new HashMap<>();
        int i = 0;
        for (String k : cacKhoa) {
            mau.put(k, i++ % SO_MAU);
        }
        return mau;
    }

    /**
     * Chia cột con trong từng ngày để các buổi cùng thời gian (ví dụ hai giảng viên dạy cùng giờ ở hai phòng)
     * nằm cạnh nhau trên lưới thay vì chồng lên nhau.
     *
     * @return thứ → số cột con của ngày, để lưới nới rộng ngày có nhiều buổi cùng giờ
     */
    public static Map<Integer, Integer> chiaCot(List<BuoiHocRow> ds) {
        Map<Integer, Integer> soCot = new HashMap<>();
        Map<Integer, List<BuoiHocRow>> theoThu = new TreeMap<>();
        for (BuoiHocRow b : ds) {
            theoThu.computeIfAbsent(b.getThu(), k -> new ArrayList<>()).add(b);
        }
        for (List<BuoiHocRow> ngay : theoThu.values()) {
            ngay.sort(Comparator.comparingInt(BuoiHocRow::getTietBatDau).thenComparing(BuoiHocRow::getMaGV));
            // tietCuoi.get(i): tiết cuối của buổi gần nhất trong cột con i
            List<Integer> tietCuoi = new ArrayList<>();
            for (BuoiHocRow b : ngay) {
                int lane = 0;
                while (lane < tietCuoi.size() && tietCuoi.get(lane) >= b.getTietBatDau()) {
                    lane++;
                }
                if (lane == tietCuoi.size()) {
                    tietCuoi.add(b.getTietKetThuc());
                } else {
                    tietCuoi.set(lane, b.getTietKetThuc());
                }
                b.setLane(lane);
            }
            for (BuoiHocRow b : ngay) {
                b.setSoLane(tietCuoi.size());
            }
            soCot.put(ngay.get(0).getThu(), tietCuoi.size());
        }
        return soCot;
    }

    // ===================== Tiện ích =====================

    /** Sinh viên đã đăng ký từng lớp (phân công) trong học kỳ. */
    private Map<PhanCongId, Set<String>> sinhVienTheoLop(String hocKy) {
        Map<PhanCongId, Set<String>> map = new HashMap<>();
        for (Object[] row : dangKyRepository.findSinhVienTheoLop(hocKy)) {
            PhanCongId id = new PhanCongId((String) row[0], (String) row[1], hocKy);
            map.computeIfAbsent(id, k -> new TreeSet<>()).add((String) row[2]);
        }
        return map;
    }

    private static int siSo(PhanCongId id, Map<PhanCongId, Set<String>> svTheoLop) {
        return svTheoLop.getOrDefault(id, Set.of()).size();
    }

    /** @param hk học kỳ để tính ngày học đầu và cuối, null thì không có ngày */
    private BuoiHocRow toRow(LichHoc l, HocKy hk, String xungDot, int mau, Map<PhanCongId, Set<String>> svTheoLop) {
        PhanCong pc = l.getPhanCong();
        LocalDate ngayDau = hk == null ? null : hk.ngayHoc(l.getTuanBatDau(), l.getThu());
        LocalDate ngayCuoi = hk == null ? null : hk.ngayHoc(l.getTuanKetThuc(), l.getThu());
        return new BuoiHocRow(l.getMaLich(), maGV(l), pc.getGiangVien().getHoTen(), maMH(l),
                pc.getMonHoc().getTenMH(), l.getThu(), l.getTietBatDau(), l.getSoTiet(), l.getTuanBatDau(),
                l.getTuanKetThuc(), ngayDau, ngayCuoi, l.getPhong().getMaPhong(), l.getPhong().getSucChua(),
                siSo(pc.getId(), svTheoLop), l.isDaGhim(), l.isChinhThuc(), xungDot, mau);
    }

    private ChuaXepRow chuaXepRow(PhanCong pc, String lyDo, Map<PhanCongId, Set<String>> svTheoLop) {
        return new ChuaXepRow(pc.getId().getMaGV(), pc.getGiangVien().getHoTen(), pc.getId().getMaMH(),
                pc.getMonHoc().getTenMH(), pc.getMonHoc().getSoTinChi(), siSo(pc.getId(), svTheoLop), lyDo);
    }

    private static boolean cungLop(LichHoc a, LichHoc b) {
        return a.getPhanCong().getId().equals(b.getPhanCong().getId());
    }

    private static String maGV(LichHoc l) {
        return l.getPhanCong().getId().getMaGV();
    }

    private static String maMH(LichHoc l) {
        return l.getPhanCong().getId().getMaMH();
    }

    /** Ví dụ "thứ 3 tiết 1–4", dùng trong thông báo vi phạm. */
    private static String thuTiet(LichHoc l) {
        return "thứ " + l.getThu() + " tiết " + l.getTietBatDau() + "–" + l.getTietKetThuc();
    }
}
