package com.manha.lab0910.controller;

import com.manha.lab0910.service.TonKhoService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final TonKhoService tonKhoService;

    public HomeController(TonKhoService tonKhoService) {
        this.tonKhoService = tonKhoService;
    }

    @GetMapping("/")
    public String trangChu() {
        return "redirect:/dashboard";
    }

    /**
     * (UC1) Chỉ hiển thị trang đăng nhập.
     * Việc kiểm tra tài khoản/mật khẩu do Spring Security làm ở POST /login.
     */
    @GetMapping("/login")
    public String dangNhap(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Authentication auth,
                           HttpSession session,
                           Model model) {

        // Đã đăng nhập rồi thì khỏi vào lại trang login
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }

        if (error != null) {
            // Spring Security cất lý do đăng nhập hỏng vào session, lấy ra để báo đúng câu
            Object loiDangNhap = session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            if (loiDangNhap instanceof DisabledException) {
                model.addAttribute("loi", "Tài khoản đã bị khoá, liên hệ quản lý");
            } else {
                // Sai tài khoản hay sai mật khẩu đều báo chung một câu,
                // không nói rõ sai cái nào (đặc tả UC1 - luồng phụ 3b)
                model.addAttribute("loi", "Tên đăng nhập hoặc mật khẩu không đúng");
            }
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }

        if (logout != null) {
            model.addAttribute("thongBao", "Bạn đã đăng xuất");
        }

        return "login";
    }

    /** (5.2) Trang chủ: 4 thẻ thống kê + bảng lô cần ưu tiên xử lý. */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("soSanPham", tonKhoService.demSanPham());
        model.addAttribute("tongTon", tonKhoService.tongTonToanKho());
        model.addAttribute("soLoSapHetHan", tonKhoService.demLoSapHetHan());
        model.addAttribute("soLoQuaHan", tonKhoService.demLoQuaHan());
        model.addAttribute("loCanUuTien", tonKhoService.loCanUuTien(10));
        return "index";
    }

    @GetMapping("/403")
    public String khongCoQuyen() {
        return "403";
    }
}
