package com.dangkymonhoc.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dangkymonhoc.controller.Redirects;
import com.dangkymonhoc.dto.KhoaForm;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.service.KhoaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH13 Quản lý khoa (UC11). */
@Controller
@RequestMapping("/admin/khoa")
@RequiredArgsConstructor
public class KhoaController {

    private final KhoaService khoaService;

    @GetMapping
    public String danhSach(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("form", new KhoaForm());
        napDanhSach(model, q);
        return "admin/khoa";
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("form") KhoaForm form, BindingResult result,
                      Model model, RedirectAttributes ra) {
        if (!form.isEditMode() && !result.hasFieldErrors("maKhoa") && khoaService.tonTai(form.getMaKhoa())) {
            result.rejectValue("maKhoa", "trung", "Mã " + form.getMaKhoa() + " đã tồn tại.");
        }
        if (!result.hasErrors()) {
            try {
                String ten = khoaService.luu(form);
                ra.addFlashAttribute("successMessage",
                        (form.isEditMode() ? "Đã cập nhật khoa " : "Đã thêm khoa ") + ten + ".");
                return "redirect:/admin/khoa";
            } catch (BusinessException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        // Lỗi: hiển thị lại danh sách và tự mở lại modal
        model.addAttribute("openModal", true);
        napDanhSach(model, "");
        return "admin/khoa";
    }

    @PostMapping("/{ma}/xoa")
    public String xoa(@PathVariable String ma, HttpServletRequest request, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("successMessage", "Đã xóa khoa " + khoaService.xoa(ma) + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/khoa");
    }

    private void napDanhSach(Model model, String q) {
        model.addAttribute("q", q);
        model.addAttribute("khoas", khoaService.timKiem(q));
    }
}
