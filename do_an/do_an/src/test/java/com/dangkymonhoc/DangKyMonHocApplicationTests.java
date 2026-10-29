package com.dangkymonhoc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dangkymonhoc.common.Quyen;
import com.dangkymonhoc.repository.HocKyRepository;
import com.dangkymonhoc.repository.PhanCongRepository;
import com.dangkymonhoc.repository.PhongRepository;
import com.dangkymonhoc.repository.TaiKhoanRepository;
import com.dangkymonhoc.service.DangKyService;

/** Khởi động toàn bộ ứng dụng trên H2: kiểm tra ánh xạ entity, các câu JPQL và DataSeeder. */
@SpringBootTest
@ActiveProfiles("h2")
class DangKyMonHocApplicationTests {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;
    @Autowired
    private PhanCongRepository phanCongRepository;
    @Autowired
    private PhongRepository phongRepository;
    @Autowired
    private DangKyService dangKyService;
    @Autowired
    private HocKyRepository hocKyRepository;

    @Test
    void contextLoadsVaNapDuLieuMau() {
        assertThat(taiKhoanRepository.count()).isEqualTo(15);
        assertThat(phanCongRepository.countMonDuocMo("HK1-2026")).isEqualTo(6);
        assertThat(dangKyService.tongTinChi("SV01", "HK1-2026")).isEqualTo(6);
        assertThat(dangKyService.daDangKy("SV01", "HK1-2026")).hasSize(2);
        assertThat(phongRepository.count()).isEqualTo(6);
        assertThat(hocKyRepository.count()).isEqualTo(3);
        assertThat(taiKhoanRepository.findChiTiet("admin").orElseThrow().getQuyen()).isEqualTo(Quyen.ADMIN);
    }
}
