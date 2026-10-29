package com.dangkymonhoc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.dto.BuoiHocRow;
import com.dangkymonhoc.dto.KetQuaXepLich;
import com.dangkymonhoc.dto.LichHocForm;
import com.dangkymonhoc.entity.HocKy;
import com.dangkymonhoc.entity.LichHoc;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.PhanCongId;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.GiangVienRepository;
import com.dangkymonhoc.repository.HocKyRepository;
import com.dangkymonhoc.repository.LichHocRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;

/** Thuật toán xếp lịch trên dữ liệu mẫu (H2). Mỗi test chạy trong transaction và được rollback. */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class LichHocServiceTest {

    private static final String HK = "HK1-2026";

    @Autowired
    private LichHocService lichHocService;
    @Autowired
    private LichHocRepository lichHocRepository;
    @Autowired
    private PhanCongRepository phanCongRepository;
    @Autowired
    private MonHocRepository monHocRepository;
    @Autowired
    private GiangVienRepository giangVienRepository;
    @Autowired
    private HocKyRepository hocKyRepository;

    @Test
    void tietBatDauKhongVatQuaHaiBuoi() {
        assertThat(LichHocService.tietBatDauHopLe(3)).containsExactly(1, 2, 3, 6, 7, 8);
        assertThat(LichHocService.tietBatDauHopLe(5)).containsExactly(1, 6);
        assertThat(LichHocService.tietBatDauHopLe(6)).isEmpty();
    }

    @Test
    void xepDuMoiPhanCongKhongTrungGiangVienVaPhong() {
        KetQuaXepLich kq = lichHocService.tuDongXep(HK);

        assertThat(kq.chuaXep()).isEmpty();
        assertThat(kq.soBuoiXep()).isEqualTo(7);
        kiemTraKhongTrung(lichHocRepository.findByHocKy(HK));
    }

    @Test
    void nhieuPhanCongVanKhongTrungGiangVienVaPhong() {
        // HK3-2026 (18 tuần): GV01, GV02 cùng dạy cả 6 môn CNTT; GV03, GV04 cùng dạy 2 môn KT → 16 phân công
        String hk = "HK3-2026";
        hocKyRepository.save(new HocKy(hk, LocalDate.of(2027, 6, 7), LocalDate.of(2027, 10, 10)));
        for (MonHoc m : monHocRepository.findAll()) {
            List<String> dsGV = m.getKhoa().getMaKhoa().equals("CNTT") ? List.of("GV01", "GV02") : List.of("GV03", "GV04");
            for (String maGV : dsGV) {
                phanCongRepository.save(new PhanCong(new PhanCongId(maGV, m.getMaMH(), hk),
                        giangVienRepository.getReferenceById(maGV), m));
            }
        }

        KetQuaXepLich kq = lichHocService.tuDongXep(hk);

        List<LichHoc> ds = lichHocRepository.findByHocKy(hk);
        assertThat(kq.chuaXep()).isEmpty();
        assertThat(ds).hasSize(16);
        kiemTraKhongTrung(ds);
    }

    @Test
    void chayLaiRaCungKetQua() {
        lichHocService.tuDongXep(HK);
        List<String> lan1 = chupLich(HK);

        lichHocService.tuDongXep(HK);

        assertThat(chupLich(HK)).isEqualTo(lan1);
    }

    @Test
    void giuNguyenBuoiDaGhimKhiXepLai() {
        lichHocService.tuDongXep(HK);
        LichHoc ltw = lichHocRepository.findByPhanCong("GV01", "LTW", HK).orElseThrow();
        Integer maLich = ltw.getMaLich();

        // Sửa tay sang thứ 7 chiều, phòng C302 → tự bật ghim
        lichHocService.luuBuoi(form(ltw, 7, 6, "C302"));
        lichHocService.tuDongXep(HK);

        LichHoc sau = lichHocRepository.findByPhanCong("GV01", "LTW", HK).orElseThrow();
        assertThat(sau.getMaLich()).isEqualTo(maLich);
        assertThat(sau.getThu()).isEqualTo(7);
        assertThat(sau.getTietBatDau()).isEqualTo(6);
        assertThat(sau.getPhong().getMaPhong()).isEqualTo("C302");
        assertThat(sau.isDaGhim()).isTrue();
        assertThat(lichHocRepository.findByHocKy(HK)).hasSize(7);
        kiemTraKhongTrung(lichHocRepository.findByHocKy(HK));
    }

    @Test
    void suaTayTrungGiangVienBaoLyDoCuThe() {
        lichHocService.tuDongXep(HK);
        LichHoc csdl = lichHocRepository.findByPhanCong("GV01", "CSDL", HK).orElseThrow();
        LichHoc ltw = lichHocRepository.findByPhanCong("GV01", "LTW", HK).orElseThrow();

        LichHocForm form = form(ltw, csdl.getThu(), csdl.getTietBatDau(), "C302");

        assertThatThrownBy(() -> lichHocService.luuBuoi(form))
                .isInstanceOf(BusinessException.class)
                .hasMessage("GV01 đã dạy CSDL thứ " + csdl.getThu() + " tiết " + csdl.getTietBatDau() + "–"
                        + csdl.getTietKetThuc() + ".");
    }

    @Test
    void suaTayTrungPhongBaoLyDoCuThe() {
        lichHocService.tuDongXep(HK);
        LichHoc csdl = lichHocRepository.findByPhanCong("GV01", "CSDL", HK).orElseThrow();
        LichHoc ktvm = lichHocRepository.findByPhanCong("GV03", "KTVM", HK).orElseThrow();
        String phong = csdl.getPhong().getMaPhong();

        LichHocForm form = form(ktvm, csdl.getThu(), csdl.getTietBatDau(), phong);

        assertThatThrownBy(() -> lichHocService.luuBuoi(form))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Phòng " + phong + " đã có lớp CSDL (GV01) thứ " + csdl.getThu() + " tiết "
                        + csdl.getTietBatDau() + "–" + csdl.getTietKetThuc() + ".");
    }

    @Test
    void suaTayKhongDuocVatQuaHaiBuoi() {
        lichHocService.tuDongXep(HK);
        LichHoc ltw = lichHocRepository.findByPhanCong("GV01", "LTW", HK).orElseThrow();

        assertThatThrownBy(() -> lichHocService.luuBuoi(form(ltw, 4, 4, "C302")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vắt qua hai buổi");
    }

    @Test
    void luuChinhThucRoiSuaPhaiXacNhan() {
        lichHocService.tuDongXep(HK);
        assertThat(lichHocService.luuChinhThuc(HK)).isEqualTo(7);
        assertThat(lichHocRepository.findByHocKy(HK)).allMatch(LichHoc::isChinhThuc);

        LichHoc ltw = lichHocRepository.findByPhanCong("GV01", "LTW", HK).orElseThrow();
        LichHocForm form = form(ltw, 7, 6, "C302");
        assertThatThrownBy(() -> lichHocService.luuBuoi(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageStartingWith("Lịch học kỳ đã chính thức");

        form.setXacNhan(true);
        lichHocService.luuBuoi(form);
        assertThat(ltw.getThu()).isEqualTo(7);
        assertThat(ltw.isChinhThuc()).isTrue();

        // Buổi chính thức không bị tự động xếp di chuyển
        lichHocService.tuDongXep(HK);
        assertThat(lichHocRepository.findByPhanCong("GV01", "LTW", HK).orElseThrow().getThu()).isEqualTo(7);
    }

    @Test
    void tuDongXepHocTuan1Den15VaTinhNgayHoc() {
        lichHocService.tuDongXep(HK);

        assertThat(lichHocRepository.findByHocKy(HK))
                .allMatch(l -> l.getTuanBatDau() == 1 && l.getTuanKetThuc() == 15);
        // HK1-2026 bắt đầu thứ 2 07/09/2026: buổi thứ 3 tuần 1 là 08/09, tuần 15 là 15/12
        BuoiHocRow buoi = new BuoiHocRow(null, "GV01", "", "LTW", "", 3, 1, 3, 1, 15, null, null, "A101", 40, 0,
                false, true, null, 0);
        assertThat(LichHocService.locTheoTuan(List.of(buoi), 16)).isEmpty();
        assertThat(LichHocService.locTheoTuan(List.of(buoi), 15)).hasSize(1);
        assertThat(lichHocService.lichCuaGiangVien("GV01", HK)).isEmpty();

        lichHocService.luuChinhThuc(HK);
        BuoiHocRow ltw = lichHocService.lichCuaGiangVien("GV01", HK).stream()
                .filter(b -> b.getMaMH().equals("LTW")).findFirst().orElseThrow();
        LocalDate thuHaiTuan1 = LocalDate.of(2026, 9, 7);
        assertThat(ltw.getNgayDau()).isEqualTo(thuHaiTuan1.plusDays(ltw.getThu() - 2L));
        assertThat(ltw.getNgayCuoi()).isEqualTo(ltw.getNgayDau().plusWeeks(14));
    }

    @Test
    void suaTayTuanVuotQuaHocKyBaoLoi() {
        lichHocService.tuDongXep(HK);
        LichHoc ltw = lichHocRepository.findByPhanCong("GV01", "LTW", HK).orElseThrow();

        // HK1-2026 có 18 tuần, học 15 tuần từ tuần 5 thì kết thúc tuần 19
        LichHocForm form = form(ltw, 7, 6, "C302");
        form.setTuanBatDau(5);
        assertThatThrownBy(() -> lichHocService.luuBuoi(form))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Lớp học tuần 5–19 nhưng học kỳ chỉ có 18 tuần.");

        form.setTuanBatDau(4);
        lichHocService.luuBuoi(form);
        assertThat(ltw.getTuanBatDau()).isEqualTo(4);
        assertThat(ltw.getTuanKetThuc()).isEqualTo(18);
        assertThat(ltw.isDaGhim()).isTrue();
    }

    @Test
    void lopChungSinhVienKhongDonChungMotNgay() {
        // SV08 học cả KTVM (GV03) và NLKT (GV04): khác giảng viên nhưng nên học ở hai ngày khác nhau
        lichHocService.tuDongXep(HK);

        LichHoc ktvm = lichHocRepository.findByPhanCong("GV03", "KTVM", HK).orElseThrow();
        LichHoc nlkt = lichHocRepository.findByPhanCong("GV04", "NLKT", HK).orElseThrow();
        assertThat(ktvm.getThu()).isNotEqualTo(nlkt.getThu());
    }

    @Test
    void hocKyDaQuaChiDuocXem() {
        String hk = "HK2-2025";
        assertThat(lichHocService.choPhepXep(hk)).isFalse();
        assertThat(lichHocService.choPhepXep(HK)).isTrue();
        assertThat(lichHocService.choPhepXep("HK3-2026")).isTrue();

        assertThatThrownBy(() -> lichHocService.tuDongXep(hk))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã qua");
        assertThatThrownBy(() -> lichHocService.luuChinhThuc(hk))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã qua");

        LichHocForm form = new LichHocForm();
        form.setMaGV("GV01");
        form.setMaMH("CTDL");
        form.setHocKy(hk);
        form.setThu(2);
        form.setTietBatDau(1);
        form.setMaPhong("C302");
        assertThatThrownBy(() -> lichHocService.luuBuoi(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã qua");
        assertThat(lichHocRepository.findByHocKy(hk)).isEmpty();
    }

    /** Mọi cặp buổi giao nhau phải khác giảng viên và khác phòng; mọi buổi nằm trọn trong sáng hoặc chiều. */
    private static void kiemTraKhongTrung(List<LichHoc> ds) {
        for (LichHoc l : ds) {
            assertThat(LichHocService.tietHopLe(l.getTietBatDau(), l.getSoTiet())).isTrue();
        }
        for (int i = 0; i < ds.size(); i++) {
            for (int j = i + 1; j < ds.size(); j++) {
                LichHoc a = ds.get(i);
                LichHoc b = ds.get(j);
                if (a.giaoNhau(b)) {
                    assertThat(a.getPhanCong().getId().getMaGV())
                            .as("Trùng giảng viên: %s và %s", a.getMoTa(), b.getMoTa())
                            .isNotEqualTo(b.getPhanCong().getId().getMaGV());
                    assertThat(a.getPhong().getMaPhong())
                            .as("Trùng phòng: %s và %s", a.getMoTa(), b.getMoTa())
                            .isNotEqualTo(b.getPhong().getMaPhong());
                }
            }
        }
    }

    /** Lịch học kỳ dạng chuỗi "GV01 CSDL Thứ 2, tiết 1–3, P.A101", sắp theo giảng viên và môn. */
    private List<String> chupLich(String hocKy) {
        List<String> ds = new ArrayList<>();
        for (LichHoc l : lichHocRepository.findByHocKy(hocKy)) {
            ds.add(l.getPhanCong().getId().getMaGV() + " " + l.getPhanCong().getId().getMaMH() + " " + l.getMoTa());
        }
        ds.sort(null);
        return ds;
    }

    private static LichHocForm form(LichHoc l, int thu, int tiet, String maPhong) {
        LichHocForm form = new LichHocForm();
        form.setMaLich(l.getMaLich());
        form.setMaGV(l.getPhanCong().getId().getMaGV());
        form.setMaMH(l.getPhanCong().getId().getMaMH());
        form.setHocKy(l.getPhanCong().getId().getHocKy());
        form.setThu(thu);
        form.setTietBatDau(tiet);
        form.setMaPhong(maPhong);
        return form;
    }
}
