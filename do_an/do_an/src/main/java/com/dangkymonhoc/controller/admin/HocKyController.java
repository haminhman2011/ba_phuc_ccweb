package com.dangkymonhoc.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dangkymonhoc.dto.HocKyForm;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.service.HocKyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH23 Quản lý học kỳ: mã học kỳ và thời gian học (ngày bắt đầu, ngày kết thúc). */
@Controller
@RequestMapping("/admin/hoc-ky")
@RequiredArgsConstructor
public class HocKyController {

    private final HocKyService hocKyService;

    @GetMapping
    public String danhSach(Model model) {
        model.addAttribute("form", new HocKyForm());
        model.addAttribute("hocKys", hocKyService.danhSach());
        return "admin/hoc-ky";
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("form") HocKyForm form, BindingResult result,
                      Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                String ma = hocKyService.luu(form);
                ra.addFlashAttribute("successMessage",
                        (form.isEditMode() ? "Đã cập nhật học kỳ " : "Đã thêm học kỳ ") + ma + ".");
                return "redirect:/admin/hoc-ky";
            } catch (BusinessException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        // Lỗi: hiển thị lại danh sách và tự mở lại modal
        model.addAttribute("openModal", true);
        model.addAttribute("hocKys", hocKyService.danhSach());
        return "admin/hoc-ky";
    }

    @PostMapping("/{ma}/xoa")
    public String xoa(@PathVariable String ma, RedirectAttributes ra) {
        try {
            hocKyService.xoa(ma);
            ra.addFlashAttribute("successMessage", "Đã xóa học kỳ " + ma + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/hoc-ky";
    }
}
