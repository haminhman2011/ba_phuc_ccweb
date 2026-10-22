package com.dangkymonhoc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dangkymonhoc.security.RoleBasedSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {

    /** Trang chủ chung: chuyển về trang chủ theo vai trò. */
    @GetMapping("/")
    public String home(Authentication authentication) {
        return "redirect:" + RoleBasedSuccessHandler.trangChu(authentication);
    }

    /** Trang 403 khi Spring Security từ chối truy cập theo URL. */
    @RequestMapping("/403")
    public String accessDenied(HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return "error/403";
    }
}
