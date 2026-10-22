package com.dangkymonhoc.config;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.entity.DangKyId;
import com.dangkymonhoc.entity.GiangVien;
import com.dangkymonhoc.entity.Khoa;
import com.dangkymonhoc.entity.Lop;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.PhanCongId;
import com.dangkymonhoc.entity.Quyen;
import com.dangkymonhoc.entity.SinhVien;
import com.dangkymonhoc.entity.TaiKhoan;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.GiangVienRepository;
import com.dangkymonhoc.repository.KhoaRepository;
import com.dangkymonhoc.repository.LopRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;
import com.dangkymonhoc.repository.QuyenRepository;
import com.dangkymonhoc.repository.SinhVienRepository;
import com.dangkymonhoc.repository.TaiKhoanRepository;

import lombok.RequiredArgsConstructor;

/**
 * Nạp dữ liệu mẫu khi bảng QUYEN còn rỗng (chưa import database/dang_ky_mon_hoc.sql).
 * Dữ liệu ở đây giống hệt phần INSERT trong file SQL.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final QuyenRepository quyenRepository;
    private final KhoaRepository khoaRepository;
    private final LopRepository lopRepository;
    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final MonHocRepository monHocRepository;
    private final PhanCongRepository phanCongRepository;
    private final DangKyRepository dangKyRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, Khoa> khoa = new HashMap<>();
    private final Map<String, Lop> lop = new HashMap<>();
    private final Map<String, SinhVien> sv = new HashMap<>();
    private final Map<String, GiangVien> gv = new HashMap<>();
    private final Map<String, MonHoc> mh = new HashMap<>();

    @Override
    @Transactional
    public void run(String... args) {
        if (quyenRepository.count() > 0) {
            return;
        }
        log.info("CSDL trống, đang nạp dữ liệu mẫu...");

        Quyen admin = quyenRepository.save(new Quyen(Quyen.ADMIN, "Quản trị viên"));
        Quyen quyenGv = quyenRepository.save(new Quyen(Quyen.GV, "Giảng viên"));
        Quyen quyenSv = quyenRepository.save(new Quyen(Quyen.SV, "Sinh viên"));

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

        // SV01: 2 môn trong học kỳ hiện tại, CSDL đã có điểm (không hủy được - QĐ6)
        dangKy("SV01", "CSDL", "HK1-2026", "2026-09-05", 8.5f);
        dangKy("SV01", "LTW", "HK1-2026", "2026-09-05", null);
        dangKy("SV02", "CSDL", "HK1-2026", "2026-09-06", null);
        dangKy("SV02", "LTJ", "HK1-2026", "2026-09-06", null);
        dangKy("SV03", "CSDL", "HK1-2026", "2026-09-06", null);
        dangKy("SV03", "LTW", "HK1-2026", "2026-09-07", null);
        dangKy("SV04", "LTW", "HK1-2026", "2026-09-07", null);
        dangKy("SV05", "LTJ", "HK1-2026", "2026-09-08", null);
        dangKy("SV08", "KTVM", "HK1-2026", "2026-09-08", null);
        dangKy("SV08", "NLKT", "HK1-2026", "2026-09-08", null);
        dangKy("SV09", "NLKT", "HK1-2026", "2026-09-09", null);
        dangKy("SV01", "CTDL", "HK2-2025", "2026-01-10", 7.5f);
        dangKy("SV01", "KTVM", "HK2-2025", "2026-01-10", 4.0f);
        dangKy("SV01", "MMT", "HK2-2025", "2026-01-11", 6.0f);
        dangKy("SV02", "CTDL", "HK2-2025", "2026-01-12", 9.0f);
        dangKy("SV08", "KTVM", "HK2-2025", "2026-01-12", 6.5f);

        taiKhoanRepository.save(new TaiKhoan("admin", passwordEncoder.encode("admin123"), true, admin, null, null));
        String macDinh = passwordEncoder.encode("123456");
        gv.values().forEach(g -> taiKhoanRepository.save(
                new TaiKhoan(g.getMaGV().toLowerCase(), macDinh, true, quyenGv, null, g)));
        // sv10 bị khóa sẵn để thử QĐ9
        sv.values().forEach(s -> taiKhoanRepository.save(
                new TaiKhoan(s.getMaSV().toLowerCase(), macDinh, !"SV10".equals(s.getMaSV()), quyenSv, s, null)));

        log.info("Đã nạp dữ liệu mẫu: admin/admin123, gv01/123456, sv01/123456.");
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

    private void dangKy(String maSV, String maMH, String hocKy, String ngay, Float diem) {
        dangKyRepository.save(new DangKy(new DangKyId(maSV, maMH, hocKy), sv.get(maSV), mh.get(maMH),
                LocalDate.parse(ngay), diem));
    }
}
