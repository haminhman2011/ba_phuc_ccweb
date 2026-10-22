package com.dangkymonhoc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.entity.DangKyId;
import com.dangkymonhoc.entity.Khoa;
import com.dangkymonhoc.entity.MonHoc;
import com.dangkymonhoc.entity.SinhVien;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.repository.DangKyRepository;
import com.dangkymonhoc.repository.MonHocRepository;
import com.dangkymonhoc.repository.PhanCongRepository;
import com.dangkymonhoc.repository.SinhVienRepository;

@ExtendWith(MockitoExtension.class)
class DangKyServiceTest {

    private static final String SV = "SV01";
    private static final String HK = "HK1-2026";

    @Mock
    private DangKyRepository dangKyRepository;
    @Mock
    private PhanCongRepository phanCongRepository;
    @Mock
    private MonHocRepository monHocRepository;
    @Mock
    private SinhVienRepository sinhVienRepository;

    private DangKyService service;
    private final MonHoc csdl = new MonHoc("CSDL", "Cơ sở dữ liệu", 3, new Khoa("CNTT", "Công nghệ thông tin"));

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties();
        props.setTinChiToiDa(24);
        service = new DangKyService(dangKyRepository, phanCongRepository, monHocRepository, sinhVienRepository, props);
    }

    @Test
    void dangKyThanhCong() {
        when(phanCongRepository.countByMonHocAndHocKy("CSDL", HK)).thenReturn(1L);
        when(monHocRepository.findById("CSDL")).thenReturn(Optional.of(csdl));
        when(dangKyRepository.existsById(new DangKyId(SV, "CSDL", HK))).thenReturn(false);
        when(dangKyRepository.tongTinChi(SV, HK)).thenReturn(18L);
        when(sinhVienRepository.getReferenceById(SV)).thenReturn(new SinhVien());
        when(dangKyRepository.save(any(DangKy.class))).thenAnswer(inv -> inv.getArgument(0));

        DangKy dk = service.dangKy(SV, "CSDL", HK);

        assertThat(dk.getId()).isEqualTo(new DangKyId(SV, "CSDL", HK));
        assertThat(dk.getNgayDangKy()).isEqualTo(LocalDate.now());
        assertThat(dk.getDiem()).isNull();
        verify(dangKyRepository).save(any(DangKy.class));
    }

    @Test
    void khongDangKyMonChuaMo() {
        when(phanCongRepository.countByMonHocAndHocKy("MMT", HK)).thenReturn(0L);

        assertThatThrownBy(() -> service.dangKy(SV, "MMT", HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Môn học này chưa được mở trong học kỳ.");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongDangKyTrungMon() {
        when(phanCongRepository.countByMonHocAndHocKy("CSDL", HK)).thenReturn(1L);
        when(monHocRepository.findById("CSDL")).thenReturn(Optional.of(csdl));
        when(dangKyRepository.existsById(new DangKyId(SV, "CSDL", HK))).thenReturn(true);

        assertThatThrownBy(() -> service.dangKy(SV, "CSDL", HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bạn đã đăng ký môn Cơ sở dữ liệu trong học kỳ này.");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongDangKyVuotTinChi() {
        when(phanCongRepository.countByMonHocAndHocKy("CSDL", HK)).thenReturn(1L);
        when(monHocRepository.findById("CSDL")).thenReturn(Optional.of(csdl));
        when(dangKyRepository.existsById(new DangKyId(SV, "CSDL", HK))).thenReturn(false);
        when(dangKyRepository.tongTinChi(SV, HK)).thenReturn(22L);

        assertThatThrownBy(() -> service.dangKy(SV, "CSDL", HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Vượt quá 24 tín chỉ. Hiện có 22 tín chỉ, môn này 3 tín chỉ.");
        verify(dangKyRepository, never()).save(any());
    }

    @Test
    void khongHuyMonDaCoDiem() {
        DangKy dk = new DangKy(new DangKyId(SV, "CSDL", HK), new SinhVien(), csdl, LocalDate.now(), 8.5f);
        when(dangKyRepository.findChiTiet(SV, "CSDL", HK)).thenReturn(Optional.of(dk));

        assertThatThrownBy(() -> service.huy(SV, "CSDL", HK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Môn đã có điểm, không thể hủy.");
        verify(dangKyRepository, never()).delete(any());
    }

    @Test
    void huyMonChuaCoDiem() {
        DangKy dk = new DangKy(new DangKyId(SV, "CSDL", HK), new SinhVien(), csdl, LocalDate.now(), null);
        when(dangKyRepository.findChiTiet(SV, "CSDL", HK)).thenReturn(Optional.of(dk));

        assertThat(service.huy(SV, "CSDL", HK)).isEqualTo("Cơ sở dữ liệu");
        verify(dangKyRepository).delete(dk);
    }
}
