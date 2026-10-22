package com.dangkymonhoc.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dangkymonhoc.dto.PhanCongForm;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.service.GiangVienService;
import com.dangkymonhoc.service.HocKyService;
import com.dangkymonhoc.service.KhoaService;
import com.dangkymonhoc.service.MonHocService;
import com.dangkymonhoc.service.PhanCongService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH18 Phân công giảng dạy (UC15). */
@Controller
@RequestMapping("/admin/phan-cong")
@RequiredArgsConstructor
public class PhanCongController {

    private final PhanCongService phanCongService;
    private final HocKyService hocKyService;
    private final MonHocService monHocService;
    private final GiangVienService giangVienService;
    private final KhoaService khoaService;

    @GetMapping
    public String danhSach(@RequestParam(required = false) String hocKy, Model model) {
        String hk = hocKy == null || hocKy.isBlank() ? hocKyService.hienTai() : hocKy.trim().toUpperCase();
        if (!hocKyService.hopLe(hk)) {
            model.addAttribute("errorMessage", "Học kỳ \"" + hocKy + "\" không đúng mẫu HK{1|2|3}-{năm}, ví dụ HK1-2026.");
            hk = hocKyService.hienTai();
        }
        PhanCongForm form = new PhanCongForm();
        form.setHocKy(hk);
        model.addAttribute("form", form);
        model.addAttribute("hocKy", hk);
        model.addAttribute("dsHocKy", hocKyService.tatCa(hk));
        model.addAttribute("phanCongs", phanCongService.theoHocKy(hk));
        model.addAttribute("soSinhVien", phanCongService.soSinhVienTheoMon(hk));
        model.addAttribute("monHocs", monHocService.tatCa());
        model.addAttribute("khoas", khoaService.tatCa());
        model.addAttribute("giangViens", giangVienService.tatCa());
        return "admin/phan-cong";
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("form") PhanCongForm form, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorMessage", result.getAllErrors().get(0).getDefaultMessage());
        } else {
            try {
                ra.addFlashAttribute("successMessage", phanCongService.them(form));
            } catch (BusinessException e) {
                ra.addFlashAttribute("errorMessage", e.getMessage());
            }
        }
        ra.addAttribute("hocKy", form.getHocKy());
        return "redirect:/admin/phan-cong";
    }

    @PostMapping("/xoa")
    public String xoa(@RequestParam String maGV, @RequestParam String maMH, @RequestParam String hocKy,
                      RedirectAttributes ra) {
        try {
            phanCongService.xoa(maGV, maMH, hocKy);
            ra.addFlashAttribute("successMessage", "Đã xóa phân công " + maGV + " – " + maMH + " học kỳ " + hocKy + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        ra.addAttribute("hocKy", hocKy);
        return "redirect:/admin/phan-cong";
    }
}
