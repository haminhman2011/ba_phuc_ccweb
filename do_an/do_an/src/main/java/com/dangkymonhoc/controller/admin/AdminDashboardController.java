package com.dangkymonhoc.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.dangkymonhoc.service.HocKyService;
import com.dangkymonhoc.service.ThongKeService;

import lombok.RequiredArgsConstructor;

/** MH12 Tổng quan. */
@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ThongKeService thongKeService;
    private final HocKyService hocKyService;

    @GetMapping("/admin")
    public String dashboard(Model model) {
        String hocKy = hocKyService.hienTai();
        model.addAttribute("soSinhVien", thongKeService.soSinhVien());
        model.addAttribute("soGiangVien", thongKeService.soGiangVien());
        model.addAttribute("soMonHoc", thongKeService.soMonHoc());
        model.addAttribute("soMonDuocMo", thongKeService.soMonDuocMo(hocKy));
        model.addAttribute("soLuotDangKy", thongKeService.soLuotDangKy(hocKy));
        model.addAttribute("topMon", thongKeService.topMon(hocKy, 5));
        model.addAttribute("monChuaCoDangKy", thongKeService.monMoChuaCoDangKy(hocKy));
        return "admin/dashboard";
    }
}
