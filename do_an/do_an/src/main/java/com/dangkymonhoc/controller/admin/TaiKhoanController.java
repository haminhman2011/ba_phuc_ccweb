package com.dangkymonhoc.controller.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.dangkymonhoc.dto.TaiKhoanForm;
import com.dangkymonhoc.entity.Quyen;
import com.dangkymonhoc.exception.BusinessException;
import com.dangkymonhoc.security.CustomUserDetails;
import com.dangkymonhoc.service.TaiKhoanService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH19 Quản lý tài khoản (UC10). */
@Controller
@RequestMapping("/admin/tai-khoan")
@RequiredArgsConstructor
public class TaiKhoanController {

    private final TaiKhoanService taiKhoanService;

    @GetMapping
    public String danhSach(@RequestParam(defaultValue = "") String maQuyen,
                           @RequestParam(defaultValue = "") String trangThai,
                           @RequestParam(defaultValue = "") String q,
                           @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("maQuyen", maQuyen);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("q", q);
        model.addAttribute("quyens", taiKhoanService.dsQuyen());
        model.addAttribute("page", taiKhoanService.timKiem(maQuyen, trangThai, q, page));
        return "admin/tai-khoan/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        return hienForm(new TaiKhoanForm(), model);
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute("form") TaiKhoanForm form, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (!result.hasFieldErrors("tenDangNhap") && taiKhoanService.tonTai(form.getTenDangNhap())) {
            result.rejectValue("tenDangNhap", "trung", "Tên đăng nhập " + form.getTenDangNhap() + " đã tồn tại.");
        }
        if (!result.hasFieldErrors("nhapLaiMatKhau") && !form.getNhapLaiMatKhau().equals(form.getMatKhau())) {
            result.rejectValue("nhapLaiMatKhau", "khongKhop", "Mật khẩu nhập lại không khớp.");
        }
        if (Quyen.SV.equals(form.getMaQuyen()) && isBlank(form.getMaSV())) {
            result.rejectValue("maSV", "batBuoc", "Vui lòng chọn sinh viên sở hữu tài khoản.");
        }
        if (Quyen.GV.equals(form.getMaQuyen()) && isBlank(form.getMaGV())) {
            result.rejectValue("maGV", "batBuoc", "Vui lòng chọn giảng viên sở hữu tài khoản.");
        }
        if (!result.hasErrors()) {
            try {
                taiKhoanService.them(form);
                ra.addFlashAttribute("successMessage", "Đã tạo tài khoản " + form.getTenDangNhap() + ".");
                return "redirect:/admin/tai-khoan";
            } catch (BusinessException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        return hienForm(form, model);
    }

    @PostMapping("/{ten}/khoa")
    public String doiTrangThai(@PathVariable String ten, @AuthenticationPrincipal CustomUserDetails user,
                               HttpServletRequest request, RedirectAttributes ra) {
        try {
            boolean hoatDong = taiKhoanService.doiTrangThai(ten, user.getUsername());
            ra.addFlashAttribute("successMessage", (hoatDong ? "Đã mở khóa tài khoản " : "Đã khóa tài khoản ") + ten + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/tai-khoan");
    }

    @PostMapping("/{ten}/dat-lai-mat-khau")
    public String datLaiMatKhau(@PathVariable String ten, HttpServletRequest request, RedirectAttributes ra) {
        try {
            taiKhoanService.datLaiMatKhau(ten);
            ra.addFlashAttribute("successMessage", "Đã đặt lại mật khẩu cho " + ten + " về "
                    + TaiKhoanService.MAT_KHAU_MAC_DINH + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/tai-khoan");
    }

    @PostMapping("/{ten}/xoa")
    public String xoa(@PathVariable String ten, @AuthenticationPrincipal CustomUserDetails user,
                      HttpServletRequest request, RedirectAttributes ra) {
        try {
            taiKhoanService.xoa(ten, user.getUsername());
            ra.addFlashAttribute("successMessage", "Đã xóa tài khoản " + ten + ".");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return Redirects.back(request, "/admin/tai-khoan");
    }

    private String hienForm(TaiKhoanForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("quyens", taiKhoanService.dsQuyen());
        model.addAttribute("sinhViens", taiKhoanService.sinhVienChuaCoTaiKhoan());
        model.addAttribute("giangViens", taiKhoanService.giangVienChuaCoTaiKhoan());
        return "admin/tai-khoan/form";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
