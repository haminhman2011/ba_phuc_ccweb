package com.dangkymonhoc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dangkymonhoc.repository.PhanCongRepository;
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
    private DangKyService dangKyService;

    @Test
    void contextLoadsVaNapDuLieuMau() {
        assertThat(taiKhoanRepository.count()).isEqualTo(15);
        assertThat(phanCongRepository.countMonDuocMo("HK1-2026")).isEqualTo(6);
        assertThat(dangKyService.tongTinChi("SV01", "HK1-2026")).isEqualTo(6);
        assertThat(dangKyService.daDangKy("SV01", "HK1-2026")).hasSize(2);
    }
}
