package com.dangkymonhoc.controller.gv;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dangkymonhoc.security.CustomUserDetails;
import com.dangkymonhoc.service.DiemService;
import com.dangkymonhoc.service.GiangVienService;
import com.dangkymonhoc.service.HocKyService;

import lombok.RequiredArgsConstructor;

/** MH09 Trang chủ giảng viên và MH10 Môn được phân công (UC08). */
@Controller
@RequiredArgsConstructor
public class GvHomeController {

    private final GiangVienService giangVienService;
    private final DiemService diemService;
    private final HocKyService hocKyService;

    @GetMapping("/gv")
    public String home(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        String hocKy = hocKyService.hienTai();
        model.addAttribute("giangVien", giangVienService.layChiTiet(user.getMaGV()));
        model.addAttribute("soMon", diemService.soMonPhanCong(user.getMaGV(), hocKy));
        model.addAttribute("soSinhVien", diemService.soSinhVien(user.getMaGV(), hocKy));
        model.addAttribute("soChuaCoDiem", diemService.soSinhVienChuaCoDiem(user.getMaGV(), hocKy));
        return "gv/home";
    }

    @GetMapping("/gv/phan-cong")
    public String phanCong(@RequestParam(required = false) String hocKy,
                           @AuthenticationPrincipal CustomUserDetails user, Model model) {
        String hk = hocKyService.chuanHoa(hocKy);
        model.addAttribute("hocKy", hk);
        model.addAttribute("dsHocKy", hocKyService.sapXep(diemService.hocKyCuaGiangVien(user.getMaGV())));
        model.addAttribute("cards", diemService.monDuocPhanCong(user.getMaGV(), hk));
        return "gv/phan-cong";
    }
}
