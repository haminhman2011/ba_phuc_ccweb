package com.dangkymonhoc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.entity.DangKyId;
import com.dangkymonhoc.entity.GiangVien;
import com.dangkymonhoc.entity.Khoa;
import com.dangkymonhoc.entity.LichHoc;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.PhanCong;
import com.dangkymonhoc.entity.PhanCongId;
import com.dangkymonhoc.entity.Phong;
import com.dangkymonhoc.entity.SinhVien;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.HocKyRepository;
import com.dangkymonhoc.repository.LichHocRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;
import com.dangkymonhoc.repository.SinhVienRepository;

@ExtendWith(MockitoExtension.class)
class DangKyServiceTest {

    private static final String SV = "SV01";
    private static final String GV = "GV01";
    private static final String HK = "HK1-2026";

    @Mock
    private DangKyRepository dangKyRepository;
    @Mock
    private PhanCongRepository phanCongRepository;
    @Mock
    private MonHocRepository monHocRepository;
    @Mock
    private SinhVienRepository sinhVienRepository;
    @Mock
    private LichHocRepository lichHocRepository;
    @Mock
    private HocKyRepository hocKyRepository;

    private DangKyService service;
    private final Khoa cntt = new Khoa("CNTT", "Công nghệ thông tin");
    private final MonHoc csdl = new MonHoc("CSDL", "Cơ sở dữ liệu", 3, cntt);
    private final MonHoc ltw = new MonHoc("LTW", "Lập trình web", 3, cntt);
    private final GiangVien gv01 = new GiangVien(GV, "Trần Văn Hùng", "Tiến sĩ", cntt);
    private final Phong a101 = new Phong("A101", "Phòng A101", 40);

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.setTinChiToiDa(24);
        props.setHocKyHienTai(HK);
        HocKyService hocKyService = new HocKyService(hocKyRepository, phanCongRepository, lichHocRepository, props);
        service = new DangKyService(dangKyRepository, phanCongRepository, monHocRepository, sinhVienRepository,
                lichHocRepository, props, hocKyService);
    }

    /** Buổi học chính thức tuần 1–15 của lớp GV01 dạy môn {@code mon}. */
    private LichHoc lichChinhThuc(MonHoc mon, int thu, int tiet) {
        PhanCong pc = new PhanCong(new PhanCongId(GV, mon.getMaMH(), HK), gv01, mon);
        return new LichHoc(1, pc, thu, tiet, mon.getSoTinChi(), 1, 15, a101, LichHoc.CHINH_THUC, false);
    }

    /** Môn CSDL được mở, lớp GV01 tồn tại, SV01 chưa đăng ký, đang có {@code tinChi} tín chỉ. */
    private void monCsdlDuocMo(long tinChi) {
        when(phanCongRepository.countByMonHocAndHocKy("CSDL", HK)).thenReturn(2L);
        when(monHocRepository.findById("CSDL")).thenReturn(Optional.of(csdl));
        when(phanCongRepository.existsById(new PhanCongId(GV, "CSDL", HK))).thenReturn(true);
        when(dangKyRepository.existsById(new DangKyId(SV, "CSDL", HK))).thenReturn(false);
        when(dangKyRepository.tongTinChi(SV, HK)).thenReturn(tinChi);
    }

    @Test
    void dangKyThanhCong() {
        monCsdlDuocMo(18L);
        when(lichHocRepository.findByPhanCong(GV, "CSDL", HK)).thenReturn(Optional.of(lichChinhThuc(csdl, 3, 1)));
        when(lichHocRepository.findChinhThucCuaSinhVien(SV, HK)).thenReturn(List.of());
        when(dangKyRepository.countByLop(GV, "CSDL", HK)).thenReturn(10L);
        when(sinhVienRepository.getReferenceById(SV)).thenReturn(new SinhVien());
        when(dangKyRepository.save(any(DangKy.class))).thenAnswer(inv -> inv.getArgument(0));

        DangKy dk = service.dangKy(SV, "CSDL", GV, HK);

        assertThat(dk.getId()).isEqualTo(new DangKyId(SV, "CSDL", HK));
        assertThat(dk.getMaGV()).isEqualTo(GV);
        assertThat(dk.getNgayDangKy()).isEqualTo(LocalDate.now());
        assertThat(dk.getDiem()).isNull();
        verify(dangKyRepository).save(any(DangKy.class));
    }

    @Test
    void khongDangKyHocKyCu() {
        assertThatThrownBy(() -> service.dangKy(SV, "MMT", GV, "HK2-2025"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Chỉ được đăng ký môn học trong học kỳ hiện tại (HK1-2026).");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongHuyHocKyCu() {
        assertThatThrownBy(() -> service.huy(SV, "CTDL", "HK2-2025"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Chỉ được hủy đăng ký môn học trong học kỳ hiện tại (HK1-2026).");
        verify(dangKyRepository, never()).delete(any());
    }

    @Test
    void khongDangKyMonChuaMo() {
        when(phanCongRepository.countByMonHocAndHocKy("MMT", HK)).thenReturn(0L);

        assertThatThrownBy(() -> service.dangKy(SV, "MMT", GV, HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Môn học này chưa được mở trong học kỳ.");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongDangKyTrungMon() {
        when(phanCongRepository.countByMonHocAndHocKy("CSDL", HK)).thenReturn(2L);
        when(monHocRepository.findById("CSDL")).thenReturn(Optional.of(csdl));
        when(phanCongRepository.existsById(new PhanCongId(GV, "CSDL", HK))).thenReturn(true);
        when(dangKyRepository.existsById(new DangKyId(SV, "CSDL", HK))).thenReturn(true);

        assertThatThrownBy(() -> service.dangKy(SV, "CSDL", GV, HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bạn đã đăng ký môn Cơ sở dữ liệu trong học kỳ này.");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongDangKyVuotTinChi() {
        monCsdlDuocMo(22L);

        assertThatThrownBy(() -> service.dangKy(SV, "CSDL", GV, HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Vượt quá 24 tín chỉ. Hiện có 22 tín chỉ, môn này 3 tín chỉ.");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongDangKyKhiLichChuaCongBo() {
        monCsdlDuocMo(0L);
        when(lichHocRepository.findByPhanCong(GV, "CSDL", HK)).thenReturn(Optional.empty());
        when(lichHocRepository.countChinhThuc(HK)).thenReturn(0L);

        assertThatThrownBy(() -> service.dangKy(SV, "CSDL", GV, HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Lịch học kỳ HK1-2026 chưa được công bố, chưa thể đăng ký.");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongDangKyTrungLich() {
        monCsdlDuocMo(3L);
        when(lichHocRepository.findByPhanCong(GV, "CSDL", HK)).thenReturn(Optional.of(lichChinhThuc(csdl, 3, 2)));
        // Đã học LTW thứ 3 tiết 1–3, CSDL thứ 3 tiết 2–4 giao nhau
        when(lichHocRepository.findChinhThucCuaSinhVien(SV, HK)).thenReturn(List.of(lichChinhThuc(ltw, 3, 1)));

        assertThatThrownBy(() -> service.dangKy(SV, "CSDL", GV, HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Trùng lịch với môn Lập trình web (Thứ 3, tiết 1–3, tuần 1–15).");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongDangKyLopDaDu() {
        monCsdlDuocMo(0L);
        when(lichHocRepository.findByPhanCong(GV, "CSDL", HK)).thenReturn(Optional.of(lichChinhThuc(csdl, 3, 1)));
        when(lichHocRepository.findChinhThucCuaSinhVien(SV, HK)).thenReturn(List.of());
        when(dangKyRepository.countByLop(GV, "CSDL", HK)).thenReturn(40L);

        assertThatThrownBy(() -> service.dangKy(SV, "CSDL", GV, HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Lớp đã đủ 40 sinh viên, vui lòng chọn lớp khác.");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongHuyMonDaCoDiem() {
        DangKy dk = new DangKy(new DangKyId(SV, "CSDL", HK), new SinhVien(), csdl, GV, LocalDate.now(), 8.5f);
        when(dangKyRepository.findChiTiet(SV, "CSDL", HK)).thenReturn(Optional.of(dk));

        assertThatThrownBy(() -> service.huy(SV, "CSDL", HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Môn đã có điểm, không thể hủy.");
        verify(dangKyRepository, never()).delete(any());
    }

    @Test
    void huyMonChuaCoDiem() {
        DangKy dk = new DangKy(new DangKyId(SV, "CSDL", HK), new SinhVien(), csdl, GV, LocalDate.now(), null);
        when(dangKyRepository.findChiTiet(SV, "CSDL", HK)).thenReturn(Optional.of(dk));

        assertThat(service.huy(SV, "CSDL", HK)).isEqualTo("Cơ sở dữ liệu");
        verify(dangKyRepository).delete(dk);
    }
}
