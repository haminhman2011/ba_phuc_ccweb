package com.dangkymonhoc.config;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.common.Quyen;
import com.dangkymonhoc.common.TietHoc;
import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.entity.DangKyId;
import com.dangkymonhoc.entity.GiangVien;
import com.dangkymonhoc.entity.Khoa;
import com.dangkymonhoc.entity.LichHoc;
import com.dangkymonhoc.entity.Lop;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.PhanCongId;
import com.dangkymonhoc.entity.Phong;
import com.dangkymonhoc.entity.SinhVien;
import com.dangkymonhoc.entity.TaiKhoan;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.GiangVienRepository;
import com.dangkymonhoc.repository.HocKyRepository;
import com.dangkymonhoc.repository.KhoaRepository;
import com.dangkymonhoc.repository.LichHocRepository;
import com.dangkymonhoc.repository.LopRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;
import com.dangkymonhoc.repository.PhongRepository;
import com.dangkymonhoc.repository.SinhVienRepository;
import com.dangkymonhoc.repository.TaiKhoanRepository;
import com.dangkymonhoc.service.HocKyService;

import lombok.RequiredArgsConstructor;

/**
 * Nạp dữ liệu mẫu khi bảng TAI_KHOAN còn rỗng (chưa import database/dang_ky_mon_hoc.sql).
 * Dữ liệu ở đây giống hệt phần INSERT trong file SQL.
 * Với CSDL tạo từ phiên bản cũ: nạp phòng học nếu bảng PHONG rỗng, khai báo thời gian mặc định cho học kỳ
 * chưa có trong bảng HOC_KY, gán lớp (cột MaGV) cho đăng ký cũ và gán tuần học cho lịch học cũ.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final KhoaRepository khoaRepository;
    private final LopRepository lopRepository;
    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final MonHocRepository monHocRepository;
    private final PhanCongRepository phanCongRepository;
    private final DangKyRepository dangKyRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final PhongRepository phongRepository;
    private final HocKyRepository hocKyRepository;
    private final LichHocRepository lichHocRepository;
    private final HocKyService hocKyService;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, Khoa> khoa = new HashMap<>();
    private final Map<String, Lop> lop = new HashMap<>();
    private final Map<String, SinhVien> sv = new HashMap<>();
    private final Map<String, GiangVien> gv = new HashMap<>();
    private final Map<String, MonHoc> mh = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        if (taiKhoanRepository.count() == 0) {
            napDuLieuMau();
        }
        khaiBaoHocKyThieu();
        if (phongRepository.count() == 0) {
            napPhong();
        }
        ganLopChoDangKyCu();
        ganTuanChoLichCu();
    }

    private void napDuLieuMau() {
        log.info("CSDL trống, đang nạp dữ liệu mẫu...");

        // Thời gian mặc định (HocKyService.macDinh): HK2-2025 02/02–07/06/2026,
        // HK1-2026 07/09/2026–10/01/2027, HK2-2026 01/02–06/06/2027
        hocKyRepository.save(hocKyService.macDinh("HK2-2025"));
        hocKyRepository.save(hocKyService.macDinh("HK1-2026"));
        hocKyRepository.save(hocKyService.macDinh("HK2-2026"));

        khoa("CNTT", "Công nghệ thông tin");
        khoa("KT", "Kinh tế");

        lop("CNTT01", "Công nghệ thông tin 01", "K2024", "CNTT");
        lop("CNTT02", "Công nghệ thông tin 02", "K2025", "CNTT");
        lop("KT01", "Kinh tế 01", "K2024", "KT");

        sinhVien("SV01", "Nguyễn Văn An", "2005-03-15", "Nam", "CNTT01");
        sinhVien("SV02", "Trần Thị Bình", "2005-07-22", "Nữ", "CNTT01");
        sinhVien("SV03", "Lê Hoàng Cường", "2005-11-02", "Nam", "CNTT01");
        sinhVien("SV04", "Phạm Thị Dung", "2005-01-30", "Nữ", "CNTT01");
        sinhVien("SV05", "Hoàng Minh Đức", "2006-04-12", "Nam", "CNTT02");
        sinhVien("SV06", "Vũ Thị Hà", "2006-09-08", "Nữ", "CNTT02");
        sinhVien("SV07", "Đặng Quốc Huy", "2006-12-19", "Nam", "CNTT02");
        sinhVien("SV08", "Bùi Thị Lan", "2005-05-05", "Nữ", "KT01");
        sinhVien("SV09", "Đỗ Văn Minh", "2005-08-25", "Nam", "KT01");
        sinhVien("SV10", "Ngô Thị Ngọc", "2005-10-10", "Nữ", "KT01");

        giangVien("GV01", "Trần Văn Hùng", "Tiến sĩ", "CNTT");
        giangVien("GV02", "Nguyễn Thị Mai", "Thạc sĩ", "CNTT");
        giangVien("GV03", "Lê Văn Nam", "Thạc sĩ", "KT");
        giangVien("GV04", "Phạm Thị Oanh", "Phó giáo sư", "KT");

        monHoc("CSDL", "Cơ sở dữ liệu", 3, "CNTT");
        monHoc("CTDL", "Cấu trúc dữ liệu và giải thuật", 4, "CNTT");
        monHoc("LTW", "Lập trình web", 3, "CNTT");
        monHoc("LTJ", "Lập trình Java", 3, "CNTT");
        monHoc("MMT", "Mạng máy tính", 3, "CNTT");
        monHoc("PTTK", "Phân tích thiết kế hệ thống", 3, "CNTT");
        monHoc("KTVM", "Kinh tế vi mô", 2, "KT");
        monHoc("NLKT", "Nguyên lý kế toán", 3, "KT");

        // Học kỳ hiện tại: mở 6 môn (MMT, PTTK chưa mở)
        phanCong("GV01", "CSDL", "HK1-2026");
        phanCong("GV02", "CSDL", "HK1-2026");
        phanCong("GV01", "LTW", "HK1-2026");
        phanCong("GV02", "LTJ", "HK1-2026");
        phanCong("GV02", "CTDL", "HK1-2026");
        phanCong("GV03", "KTVM", "HK1-2026");
        phanCong("GV04", "NLKT", "HK1-2026");
        // Học kỳ trước, để có dữ liệu ở màn hình kết quả học tập
        phanCong("GV01", "CTDL", "HK2-2025");
        phanCong("GV02", "MMT", "HK2-2025");
        phanCong("GV03", "KTVM", "HK2-2025");

        // SV01: 2 môn trong học kỳ hiện tại, CSDL đã có điểm (không hủy được - QĐ6).
        // Môn CSDL có 2 lớp: GV01 và GV02.
        dangKy("SV01", "CSDL", "GV01", "HK1-2026", "2026-09-05", 8.5f);
        dangKy("SV01", "LTW", "GV01", "HK1-2026", "2026-09-05", null);
        dangKy("SV02", "CSDL", "GV02", "HK1-2026", "2026-09-06", null);
        dangKy("SV02", "LTJ", "GV02", "HK1-2026", "2026-09-06", null);
        dangKy("SV03", "CSDL", "GV01", "HK1-2026", "2026-09-06", null);
        dangKy("SV03", "LTW", "GV01", "HK1-2026", "2026-09-07", null);
        dangKy("SV04", "LTW", "GV01", "HK1-2026", "2026-09-07", null);
        dangKy("SV05", "LTJ", "GV02", "HK1-2026", "2026-09-08", null);
        dangKy("SV08", "KTVM", "GV03", "HK1-2026", "2026-09-08", null);
        dangKy("SV08", "NLKT", "GV04", "HK1-2026", "2026-09-08", null);
        dangKy("SV09", "NLKT", "GV04", "HK1-2026", "2026-09-09", null);
        dangKy("SV01", "CTDL", "GV01", "HK2-2025", "2026-01-10", 7.5f);
        dangKy("SV01", "KTVM", "GV03", "HK2-2025", "2026-01-10", 4.0f);
        dangKy("SV01", "MMT", "GV02", "HK2-2025", "2026-01-11", 6.0f);
        dangKy("SV02", "CTDL", "GV01", "HK2-2025", "2026-01-12", 9.0f);
        dangKy("SV08", "KTVM", "GV03", "HK2-2025", "2026-01-12", 6.5f);

        taiKhoanRepository.save(new TaiKhoan("admin", passwordEncoder.encode("admin123"), true, Quyen.ADMIN,
                null, null));
        String macDinh = passwordEncoder.encode("123456");
        gv.values().forEach(g -> taiKhoanRepository.save(
                new TaiKhoan(g.getMaGV().toLowerCase(), macDinh, true, Quyen.GV, null, g)));
        // sv10 bị khóa sẵn để thử QĐ9
        sv.values().forEach(s -> taiKhoanRepository.save(
                new TaiKhoan(s.getMaSV().toLowerCase(), macDinh, !"SV10".equals(s.getMaSV()), Quyen.SV, s, null)));

        log.info("Đã nạp dữ liệu mẫu: admin/admin123, gv01/123456, sv01/123456.");
    }

    /** 6 phòng học, sức chứa 40–80. */
    private void napPhong() {
        phongRepository.save(new Phong("A101", "Phòng A101", 40));
        phongRepository.save(new Phong("A102", "Phòng A102", 45));
        phongRepository.save(new Phong("B201", "Phòng B201", 50));
        phongRepository.save(new Phong("B202", "Phòng B202", 60));
        phongRepository.save(new Phong("C301", "Hội trường C301", 70));
        phongRepository.save(new Phong("C302", "Hội trường C302", 80));
        log.info("Đã nạp 6 phòng học.");
    }

    /** Học kỳ đã có phân công nhưng chưa có trong bảng HOC_KY (CSDL cũ): khai báo thời gian mặc định. */
    private void khaiBaoHocKyThieu() {
        for (Object[] row : phanCongRepository.countTheoHocKy()) {
            String hocKy = (String) row[0];
            if (hocKyService.dungMau(hocKy) && !hocKyRepository.existsById(hocKy)) {
                hocKyRepository.save(hocKyService.macDinh(hocKy));
                log.info("Đã khai báo thời gian mặc định cho học kỳ {}.", hocKy);
            }
        }
    }

    /** Lịch học tạo trước khi có cột TuanBatDau/TuanKetThuc: học từ tuần 1, đủ số tuần của môn. */
    private void ganTuanChoLichCu() {
        List<LichHoc> ds = lichHocRepository.findChuaCoTuan();
        for (LichHoc l : ds) {
            int soTinChi = l.getPhanCong().getMonHoc().getSoTinChi();
            l.setTuanBatDau(1);
            l.setTuanKetThuc(TietHoc.soTuanCan(soTinChi, soTinChi));
        }
        if (!ds.isEmpty()) {
            log.info("Đã gán tuần học cho {} buổi học cũ.", ds.size());
        }
    }

    /**
     * Đăng ký tạo trước khi có cột MaGV: gán vào lớp của giảng viên có mã nhỏ nhất dạy môn đó trong học kỳ.
     * Bỏ qua đăng ký mà môn không có phân công trong học kỳ.
     */
    private void ganLopChoDangKyCu() {
        int dem = 0;
        for (DangKy dk : dangKyRepository.findChuaCoLop()) {
            String maGV = null;
            for (PhanCong pc : phanCongRepository.findByHocKy(dk.getHocKy())) {
                if (pc.getMonHoc().getMaMH().equals(dk.getId().getMaMH())) {
                    maGV = pc.getGiangVien().getMaGV();
                    break;
                }
            }
            if (maGV != null) {
                dk.setMaGV(maGV);
                dem++;
            }
        }
        if (dem > 0) {
            log.info("Đã gán lớp (giảng viên) cho {} đăng ký cũ.", dem);
        }
    }

    private void khoa(String ma, String ten) {
        khoa.put(ma, khoaRepository.save(new Khoa(ma, ten)));
    }

    private void lop(String ma, String ten, String khoaHoc, String maKhoa) {
        lop.put(ma, lopRepository.save(new Lop(ma, ten, khoaHoc, khoa.get(maKhoa))));
    }

    private void sinhVien(String ma, String hoTen, String ngaySinh, String gioiTinh, String maLop) {
        sv.put(ma, sinhVienRepository.save(
                new SinhVien(ma, hoTen, LocalDate.parse(ngaySinh), gioiTinh, lop.get(maLop))));
    }

    private void giangVien(String ma, String hoTen, String hocVi, String maKhoa) {
        gv.put(ma, giangVienRepository.save(new GiangVien(ma, hoTen, hocVi, khoa.get(maKhoa))));
    }

    private void monHoc(String ma, String ten, int soTinChi, String maKhoa) {
        mh.put(ma, monHocRepository.save(new MonHoc(ma, ten, soTinChi, khoa.get(maKhoa))));
    }

    private void phanCong(String maGV, String maMH, String hocKy) {
        phanCongRepository.save(new PhanCong(new PhanCongId(maGV, maMH, hocKy), gv.get(maGV), mh.get(maMH)));
    }

    private void dangKy(String maSV, String maMH, String maGV, String hocKy, String ngay, Float diem) {
        dangKyRepository.save(new DangKy(new DangKyId(maSV, maMH, hocKy), sv.get(maSV), mh.get(maMH), maGV,
                LocalDate.parse(ngay), diem));
    }
}
