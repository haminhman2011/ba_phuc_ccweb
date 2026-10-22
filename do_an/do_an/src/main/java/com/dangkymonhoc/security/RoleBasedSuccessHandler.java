package com.dangkymonhoc.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Sau khi đăng nhập: ADMIN → /admin, GV → /gv, SV → /sv. */
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        response.sendRedirect(request.getContextPath() + trangChu(authentication));
    }

    public static String trangChu(Authentication authentication) {
        if (authentication == null) {
            return "/login";
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            switch (authority.getAuthority()) {
                case "ROLE_ADMIN":
                    return "/admin";
                case "ROLE_GV":
                    return "/gv";
                case "ROLE_SV":
                    return "/sv";
                default:
                    break;
            }
        }
        return "/login";
    }
}
