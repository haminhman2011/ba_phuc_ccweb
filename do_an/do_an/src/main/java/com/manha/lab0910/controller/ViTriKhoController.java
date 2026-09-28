package com.manha.lab0910.controller;

import com.manha.lab0910.entity.ViTriKho;
import com.manha.lab0910.service.ViTriKhoService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vi-tri-kho")
public class ViTriKhoController {

    private final ViTriKhoService viTriKhoService;

    public ViTriKhoController(ViTriKhoService viTriKhoService) {
        this.viTriKhoService = viTriKhoService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String danhSach(@RequestParam(required = false) String tuKhoa, Model model) {
        model.addAttribute("danhSach", viTriKhoService.timKiem(tuKhoa));
        model.addAttribute("tuKhoa", tuKhoa);
        return "vi-tri-kho/danh-sach";
    }

    @GetMapping("/them")
    public String formThem(Model model) {
        model.addAttribute("viTriKho", new ViTriKho());
        return "vi-tri-kho/form";
    }

    @GetMapping("/sua/{id}")
    public String formSua(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("viTriKho", viTriKhoService.timTheoId(id));
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
            return "redirect:/vi-tri-kho";
        }
        return "vi-tri-kho/form";
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("viTriKho") ViTriKho viTriKho,
                      BindingResult ketQua,
                      RedirectAttributes ra) {

        boolean themMoi = viTriKho.getId() == null;

        if (!ketQua.hasFieldErrors("maViTri")
                && viTriKhoService.maDaTonTai(viTriKho.getMaViTri(), viTriKho.getId())) {
            ketQua.rejectValue("maViTri", "trung", "Mã vị trí đã tồn tại");
        }

        if (ketQua.hasErrors()) {
            return "vi-tri-kho/form";
        }

        viTriKhoService.luu(viTriKho);
        ra.addFlashAttribute("thongBao",
                themMoi ? "Thêm vị trí thành công" : "Cập nhật vị trí thành công");
        return "redirect:/vi-tri-kho";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            viTriKhoService.xoa(id);
            ra.addFlashAttribute("thongBao", "Đã xoá vị trí");
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("loi", e.getMessage());
        }
        return "redirect:/vi-tri-kho";
    }
}
