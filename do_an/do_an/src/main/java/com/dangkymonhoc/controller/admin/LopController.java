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
import com.dangkymonhoc.dto.LopForm;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.service.KhoaService;
import com.dangkymonhoc.service.LopService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH14 Quản lý lớp (UC11). */
@Controller
@RequestMapping("/admin/lop")
@RequiredArgsConstructor
public class LopController {

    private final LopService lopService;
    private final KhoaService khoaService;

    @GetMapping
    public String danhSach(@RequestParam(defaultValue = "") String maKhoa,
                           @RequestParam(defaultValue = "") String q, Model model) {
        LopForm form = new LopForm();
        form.setMaKhoa(maKhoa);
        model.addAttribute("form", form);
        napDanhSach(model, maKhoa, q);
        return "admin/lop";
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("form") LopForm form, BindingResult result,
                      Model model, RedirectAttributes ra) {
        if (!form.isEditMode() && !result.hasFieldErrors("maLop") && lopService.tonTai(form.getMaLop())) {
            result.rejectValue("maLop", "trung", "Mã " + form.getMaLop() + " đã tồn tại.");
        }
        if (!result.hasErrors()) {
            try {
                String ten = lopService.luu(form);
                ra.addFlashAttribute("successMessage",
                        (form.isEditMode() ? "Đã cập nhật lớp " : "Đã thêm lớp ") + ten + ".");
                return "redirect:/admin/lop";
            } catch (BusinessException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        model.addAttribute("openModal", true);
        napDanhSach(model, "", "");
        return "admin/lop";
    }

    @PostMapping("/{ma}/xoa")
    public String xoa(@PathVariable String ma, HttpServletRequest request, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("successMessage", "Đã xóa lớp " + lopService.xoa(ma) + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/lop");
    }

    private void napDanhSach(Model model, String maKhoa, String q) {
        model.addAttribute("maKhoa", maKhoa);
        model.addAttribute("q", q);
        model.addAttribute("khoas", khoaService.tatCa());
        model.addAttribute("lops", lopService.timKiem(maKhoa, q));
    }
}
