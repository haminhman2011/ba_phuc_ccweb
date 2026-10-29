package com.dangkymonhoc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dangkymonhoc.common.TietHoc;
import com.dangkymonhoc.dto.HocKyForm;
import com.dangkymonhoc.entity.HocKy;
import com.dangkymonhoc.exception.BusinessException;

/** Thời gian học kỳ trên dữ liệu mẫu (H2): HK2-2025, HK1-2026, HK2-2026. */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class HocKyServiceTest {

    @Autowired
    private HocKyService hocKyService;

    @Test
    void thoiGianMacDinhVaNgayHoc() {
        HocKy hk = hocKyService.lay("HK1-2026");
        assertThat(hk.getNgayBatDau()).isEqualTo(LocalDate.of(2026, 9, 7));
        assertThat(hk.getNgayKetThuc()).isEqualTo(LocalDate.of(2027, 1, 10));
        assertThat(hk.getSoTuan()).isEqualTo(18);

        // Thứ 3 tuần 8 là 27/10/2026
        assertThat(hk.ngayHoc(8, 3)).isEqualTo(LocalDate.of(2026, 10, 27));
        assertThat(hk.tuanCua(LocalDate.of(2026, 10, 27))).isEqualTo(8);
        assertThat(hk.tuanCua(LocalDate.of(2026, 9, 6))).isEqualTo(0);

        assertThat(hocKyService.macDinh("HK2-2025").getNgayBatDau()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(hocKyService.macDinh("HK3-2025").getSoTuan()).isEqualTo(8);
    }

    @Test
    void hocKyHienTaiTheoNgay() {
        assertThat(hocKyService.theoNgay(LocalDate.of(2026, 3, 15))).isEqualTo("HK2-2025");
        assertThat(hocKyService.theoNgay(LocalDate.of(2026, 10, 27))).isEqualTo("HK1-2026");
        // Nghỉ hè: học kỳ sắp tới
        assertThat(hocKyService.theoNgay(LocalDate.of(2026, 7, 20))).isEqualTo("HK1-2026");
        // Sau học kỳ cuối cùng
        assertThat(hocKyService.theoNgay(LocalDate.of(2028, 1, 1))).isEqualTo("HK2-2026");
    }

    @Test
    void khongChoNgayBatDauKhacThuHaiHoacTrungHocKyKhac() {
        assertThatThrownBy(() -> hocKyService.luu(form("HK3-2026", LocalDate.of(2027, 6, 8), LocalDate.of(2027, 8, 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageStartingWith("Ngày bắt đầu phải là thứ 2");
        assertThatThrownBy(() -> hocKyService.luu(form("HK3-2026", LocalDate.of(2027, 5, 31), LocalDate.of(2027, 8, 1))))
                .isInstanceOf(BusinessException.class)
                .hasMessageStartingWith("Thời gian trùng với học kỳ HK2-2026");

        hocKyService.luu(form("HK3-2026", LocalDate.of(2027, 6, 7), LocalDate.of(2027, 8, 1)));
        assertThat(hocKyService.lay("HK3-2026").getSoTuan()).isEqualTo(8);
    }

    @Test
    void khongXoaHocKyDaCoPhanCong() {
        assertThatThrownBy(() -> hocKyService.xoa("HK1-2026"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đang có 7 phân công");

        hocKyService.xoa("HK2-2026");
        assertThat(hocKyService.tonTai("HK2-2026")).isFalse();
    }

    @Test
    void tietHocVaSoTuan() {
        assertThat(TietHoc.khungGio(1, 3)).isEqualTo("07:00–09:40");
        assertThat(TietHoc.khungGio(6, 10)).isEqualTo("13:00–17:30");
        assertThat(TietHoc.soTuanCan(3, 3)).isEqualTo(15);
        assertThat(TietHoc.soTuanCan(4, 4)).isEqualTo(15);
    }

    private static HocKyForm form(String ma, LocalDate batDau, LocalDate ketThuc) {
        HocKyForm form = new HocKyForm();
        form.setMaHK(ma);
        form.setNgayBatDau(batDau);
        form.setNgayKetThuc(ketThuc);
        return form;
    }
}
