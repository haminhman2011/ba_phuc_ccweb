package com.dangkymonhoc.controller.sv;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dangkymonhoc.entity.DangKy;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.security.CustomUserDetails;
import com.dangkymonhoc.service.DangKyService;
import com.dangkymonhoc.service.HocKyService;

import lombok.RequiredArgsConstructor;

/** MH07 Đăng ký môn học (UC03, UC04, UC05, UC06). */
@Controller
@RequestMapping("/sv/dang-ky")
@RequiredArgsConstructor
public class DangKyController {

    private final DangKyService dangKyService;
    private final HocKyService hocKyService;

    @GetMapping
    public String trang(@RequestParam(required = false) String hocKy,
                        @AuthenticationPrincipal CustomUserDetails user, Model model) {
        String hk = hocKyService.chuanHoa(hocKy);
        List<DangKy> daDangKy = dangKyService.daDangKy(user.getMaSV(), hk);
        model.addAttribute("hocKy", hk);
        model.addAttribute("dsHocKy", hocKyService.tatCa(dangKyService.hocKyDaDangKy(user.getMaSV()).toArray(String[]::new)));
        model.addAttribute("daDangKy", daDangKy);
        model.addAttribute("tongTinChi", daDangKy.stream().mapToInt(dk -> dk.getMonHoc().getSoTinChi()).sum());
        model.addAttribute("monMo", dangKyService.monDuocMo(hk, daDangKy));
        return "sv/dang-ky";
    }

    @PostMapping
    public String dangKy(@RequestParam String maMH, @RequestParam String hocKy,
                         @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        try {
            DangKy dk = dangKyService.dangKy(user.getMaSV(), maMH, hocKy);
            ra.addFlashAttribute("successMessage", "Đăng ký môn " + dk.getMonHoc().getTenMH() + " thành công.");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        ra.addAttribute("hocKy", hocKy);
        return "redirect:/sv/dang-ky";
    }

    @PostMapping("/huy")
    public String huy(@RequestParam String maMH, @RequestParam String hocKy,
                      @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        try {
            String tenMH = dangKyService.huy(user.getMaSV(), maMH, hocKy);
            ra.addFlashAttribute("successMessage", "Đã hủy đăng ký môn " + tenMH + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        ra.addAttribute("hocKy", hocKy);
        return "redirect:/sv/dang-ky";
    }
}
