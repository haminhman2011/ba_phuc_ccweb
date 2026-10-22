package com.dangkymonhoc.controller;

import java.util.Objects;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dangkymonhoc.dto.DoiMatKhauForm;
import com.dangkymonhoc.security.CustomUserDetails;
import com.dangkymonhoc.service.TaiKhoanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** MH01 Đăng nhập (UC01) và MH02 Đổi mật khẩu (UC02). POST /login do Spring Security xử lý. */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final Environment environment;
    private final TaiKhoanService taiKhoanService;

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        model.addAttribute("hienTaiKhoanMau", environment.matchesProfiles("dev"));
        return "auth/login";
    }

    @GetMapping("/doi-mat-khau")
    public String doiMatKhauForm(Model model) {
        model.addAttribute("form", new DoiMatKhauForm());
        return "auth/doi-mat-khau";
    }

    @PostMapping("/doi-mat-khau")
    public String doiMatKhau(@Valid @ModelAttribute("form") DoiMatKhauForm form, BindingResult result,
                             @AuthenticationPrincipal CustomUserDetails user, RedirectAttributes ra) {
        if (!result.hasFieldErrors("matKhauHienTai")
                && !taiKhoanService.dungMatKhau(user.getUsername(), form.getMatKhauHienTai())) {
            result.rejectValue("matKhauHienTai", "sai", "Mật khẩu hiện tại không đúng.");
        }
        if (!result.hasFieldErrors("matKhauMoi") && form.getMatKhauMoi().equals(form.getMatKhauHienTai())) {
            result.rejectValue("matKhauMoi", "trung", "Mật khẩu mới phải khác mật khẩu hiện tại.");
        }
        if (!result.hasFieldErrors("nhapLaiMatKhau")
                && !Objects.equals(form.getMatKhauMoi(), form.getNhapLaiMatKhau())) {
            result.rejectValue("nhapLaiMatKhau", "khongKhop", "Mật khẩu nhập lại không khớp.");
        }
        if (result.hasErrors()) {
            return "auth/doi-mat-khau";
        }
        taiKhoanService.doiMatKhau(user.getUsername(), form.getMatKhauMoi());
        ra.addFlashAttribute("successMessage", "Đổi mật khẩu thành công.");
        return "redirect:/doi-mat-khau";
    }
}
