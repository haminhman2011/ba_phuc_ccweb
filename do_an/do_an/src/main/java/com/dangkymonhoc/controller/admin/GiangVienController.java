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
import com.dangkymonhoc.dto.GiangVienForm;
import com.dangkymonhoc.entity.GiangVien;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.service.GiangVienService;
import com.dangkymonhoc.service.HocKyService;
import com.dangkymonhoc.service.KhoaService;
import com.dangkymonhoc.service.TaiKhoanService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH16 Quản lý giảng viên (UC13). */
@Controller
@RequestMapping("/admin/giang-vien")
@RequiredArgsConstructor
public class GiangVienController {

    private final GiangVienService giangVienService;
    private final KhoaService khoaService;
    private final TaiKhoanService taiKhoanService;
    private final HocKyService hocKyService;

    @GetMapping
    public String danhSach(@RequestParam(defaultValue = "") String maKhoa,
                           @RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "0") int page, Model model) {
        Page<GiangVien> ds = giangVienService.timKiem(maKhoa, q, page);
        model.addAttribute("maKhoa", maKhoa);
        model.addAttribute("q", q);
        model.addAttribute("page", ds);
        model.addAttribute("khoas", khoaService.tatCa());
        model.addAttribute("soMonPhanCong", giangVienService.soMonPhanCong(hocKyService.hienTai()));
        model.addAttribute("coTaiKhoan",
                taiKhoanService.maGVCoTaiKhoan(ds.getContent().stream().map(GiangVien::getMaGV).toList()));
        return "admin/giang-vien/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        return hienForm(new GiangVienForm(), model);
    }

    @GetMapping("/{ma}/sua")
    public String suaForm(@PathVariable String ma, Model model) {
        return hienForm(giangVienService.layForm(ma), model);
    }

    @PostMapping("/luu")
    public String luu(@Valid @ModelAttribute("form") GiangVienForm form, BindingResult result,
                      Model model, RedirectAttributes ra) {
        if (!form.isEditMode() && !result.hasFieldErrors("maGV") && giangVienService.tonTai(form.getMaGV())) {
            result.rejectValue("maGV", "trung", "Mã " + form.getMaGV() + " đã tồn tại.");
        }
        if (!result.hasErrors()) {
            try {
                String ten = giangVienService.luu(form);
                String msg = form.isEditMode() ? "Đã cập nhật giảng viên " + ten + "."
                        : "Đã thêm giảng viên " + ten + (form.isTaoTaiKhoan()
                                ? " và tạo tài khoản " + form.getMaGV().toLowerCase() + " (mật khẩu "
                                        + TaiKhoanService.MAT_KHAU_MAC_DINH + ")."
                                : ".");
                ra.addFlashAttribute("successMessage", msg);
                return "redirect:/admin/giang-vien";
            } catch (BusinessException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        return hienForm(form, model);
    }

    @PostMapping("/{ma}/xoa")
    public String xoa(@PathVariable String ma, HttpServletRequest request, RedirectAttributes ra) {
        try {
            ra.addFlashAttribute("successMessage", "Đã xóa giảng viên " + giangVienService.xoa(ma) + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/giang-vien");
    }

    @PostMapping("/{ma}/cap-tai-khoan")
    public String capTaiKhoan(@PathVariable String ma, HttpServletRequest request, RedirectAttributes ra) {
        try {
            String ten = giangVienService.capTaiKhoan(ma);
            ra.addFlashAttribute("successMessage", "Đã cấp tài khoản " + ten + " (mật khẩu "
                    + TaiKhoanService.MAT_KHAU_MAC_DINH + ").");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/giang-vien");
    }

    private String hienForm(GiangVienForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("khoas", khoaService.tatCa());
        model.addAttribute("hocVis", GiangVienForm.HOC_VI);
        return "admin/giang-vien/form";
    }
}
