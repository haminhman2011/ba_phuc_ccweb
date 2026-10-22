package com.dangkymonhoc.controller.sv;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.security.CustomUserDetails;
import com.dangkymonhoc.service.DangKyService;
import com.dangkymonhoc.service.HocKyService;
import com.dangkymonhoc.service.SinhVienService;

import lombok.RequiredArgsConstructor;

/** MH06 Trang chủ sinh viên. */
@Controller
@RequiredArgsConstructor
public class SvHomeController {

    private final SinhVienService sinhVienService;
    private final DangKyService dangKyService;
    private final HocKyService hocKyService;

    @GetMapping("/sv")
    public String home(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        String hocKy = hocKyService.hienTai();
        List<DangKy> daDangKy = dangKyService.daDangKy(user.getMaSV(), hocKy);
        model.addAttribute("sinhVien", sinhVienService.layChiTiet(user.getMaSV()));
        model.addAttribute("soMon", daDangKy.size());
        model.addAttribute("tongTinChi", daDangKy.stream().mapToInt(dk -> dk.getMonHoc().getSoTinChi()).sum());
        model.addAttribute("soMonCoDiem", dangKyService.soMonCoDiem(user.getMaSV()));
        return "sv/home";
    }
}
