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
import com.dangkymonhoc.dto.MonHocForm;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.service.HocKyService;
import com.dangkymonhoc.service.KhoaService;
import com.dangkymonhoc.service.MonHocService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH17 Quản lý môn học (UC14). */
@Controller
@RequestMapping("/admin/mon-hoc")
@RequiredArgsConstructor
public class MonHocController {

    private final MonHocService monHocService;
    private final KhoaService khoaService;
    private final HocKyService hocKyService;

    @GetMapping
    public String danhSach(@RequestParam(defaultValue = "") String maKhoa,
                           @RequestParam(defaultValue = "") String q, Model model) {
        MonHocForm form = new MonHocForm();
        form.setMaKhoa(maKhoa);
        model.addAttribute("form", form);
        napDanhSach(model, maKhoa, q);
        return "admin/mon-hoc";
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("form") MonHocForm form, BindingResult result,
                      Model model, RedirectAttributes ra) {
        if (!form.isEditMode() && !result.hasFieldErrors("maMH") && monHocService.tonTai(form.getMaMH())) {
            result.rejectValue("maMH", "trung", "Mã " + form.getMaMH() + " đã tồn tại.");
        }
        if (!result.hasErrors()) {
            try {
                String ten = monHocService.luu(form);
                ra.addFlashAttribute("successMessage",
                        (form.isEditMode() ? "Đã cập nhật môn " : "Đã thêm môn ") + ten + ".");
                return "redirect:/admin/mon-hoc";
            } catch (BusinessException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        model.addAttribute("openModal", true);
        napDanhSach(model, "", "");
        return "admin/mon-hoc";
    }

    @PostMapping("/{ma}/xoa")
    public String xoa(@PathVariable String ma, HttpServletRequest request, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("successMessage", "Đã xóa môn " + monHocService.xoa(ma) + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/mon-hoc");
    }

    private void napDanhSach(Model model, String maKhoa, String q) {
        model.addAttribute("maKhoa", maKhoa);
        model.addAttribute("q", q);
        model.addAttribute("khoas", khoaService.tatCa());
        model.addAttribute("monHocs", monHocService.timKiem(maKhoa, q));
        model.addAttribute("monDangMo", monHocService.maMonDuocMo(hocKyService.hienTai()));
    }
}
