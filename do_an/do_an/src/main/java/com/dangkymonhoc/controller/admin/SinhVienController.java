package com.dangkymonhoc.controller.admin;

import org.springframework.data.domain.Page;
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
import com.dangkymonhoc.dto.SinhVienForm;
import com.dangkymonhoc.entity.SinhVien;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.service.KhoaService;
import com.dangkymonhoc.service.LopService;
import com.dangkymonhoc.service.SinhVienService;
import com.dangkymonhoc.service.TaiKhoanService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH15 Quản lý sinh viên (UC12). */
@Controller
@RequestMapping("/admin/sinh-vien")
@RequiredArgsConstructor
public class SinhVienController {

    private final SinhVienService sinhVienService;
    private final KhoaService khoaService;
    private final LopService lopService;
    private final TaiKhoanService taiKhoanService;

    @GetMapping
    public String danhSach(@RequestParam(defaultValue = "") String maKhoa,
                           @RequestParam(defaultValue = "") String maLop,
                           @RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "0") int page, Model model) {
        Page<SinhVien> ds = sinhVienService.timKiem(maKhoa, maLop, q, page);
        model.addAttribute("maKhoa", maKhoa);
        model.addAttribute("maLop", maLop);
        model.addAttribute("q", q);
        model.addAttribute("page", ds);
        model.addAttribute("khoas", khoaService.tatCa());
        model.addAttribute("lops", lopService.theoKhoa(maKhoa));
        model.addAttribute("coTaiKhoan",
                taiKhoanService.maSVCoTaiKhoan(ds.getContent().stream().map(SinhVien::getMaSV).toList()));
        return "admin/sinh-vien/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        return hienForm(new SinhVienForm(), model);
    }

    @GetMapping("/{ma}/sua")
    public String suaForm(@PathVariable String ma, Model model) {
        return hienForm(sinhVienService.layForm(ma), model);
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("form") SinhVienForm form, BindingResult result,
                      Model model, RedirectAttributes ra) {
        if (!form.isEditMode() && !result.hasFieldErrors("maSV") && sinhVienService.tonTai(form.getMaSV())) {
            result.rejectValue("maSV", "trung", "Mã " + form.getMaSV() + " đã tồn tại.");
        }
        if (!result.hasErrors()) {
            try {
                String ten = sinhVienService.luu(form);
                String msg = form.isEditMode() ? "Đã cập nhật sinh viên " + ten + "."
                        : "Đã thêm sinh viên " + ten + (form.isTaoTaiKhoan()
                                ? " và tạo tài khoản " + form.getMaSV().toLowerCase() + " (mật khẩu "
                                        + TaiKhoanService.MAT_KHAU_MAC_DINH + ")."
                                : ".");
                ra.addFlashAttribute("successMessage", msg);
                return "redirect:/admin/sinh-vien";
            } catch (BusinessException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        return hienForm(form, model);
    }

    @PostMapping("/{ma}/xoa")
    public String xoa(@PathVariable String ma, HttpServletRequest request, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("successMessage", "Đã xóa sinh viên " + sinhVienService.xoa(ma) + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/sinh-vien");
    }

    @PostMapping("/{ma}/cap-tai-khoan")
    public String capTaiKhoan(@PathVariable String ma, HttpServletRequest request, RedirectAttributes ra) {
        try {
            String ten = sinhVienService.capTaiKhoan(ma);
            ra.addFlashAttribute("successMessage", "Đã cấp tài khoản " + ten + " (mật khẩu "
                    + TaiKhoanService.MAT_KHAU_MAC_DINH + ").");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/sinh-vien");
    }

    private String hienForm(SinhVienForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("khoas", khoaService.tatCa());
        model.addAttribute("lops", lopService.theoKhoa(form.getMaKhoa()));
        return "admin/sinh-vien/form";
    }
}
