package com.dangkymonhoc.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.util.UriUtils;

import com.dangkymonhoc.config.AppProperties;
import com.dangkymonhoc.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/** Đưa các giá trị dùng chung (học kỳ, người dùng, đường dẫn hiện tại) vào model của mọi trang. */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final AppProperties appProperties;

    @ModelAttribute("hocKyHienTai")
    public String hocKyHienTai() {
        return appProperties.getHocKyHienTai();
    }

    @ModelAttribute("tinChiToiDa")
    public int tinChiToiDa() {
        return appProperties.getTinChiToiDa();
    }

    @ModelAttribute("currentUser")
    public CustomUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user;
        }
        return null;
    }

    /** Đường dẫn hiện tại (không có context path), dùng để tô sáng menu sidebar. */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    /**
     * Tiền tố link phân trang: giữ nguyên các tham số tìm kiếm/lọc, chỉ thay "page".
     * Ví dụ "/admin/sinh-vien?q=an&amp;maKhoa=CNTT&amp;page=".
     */
    @ModelAttribute("pageUrlPrefix")
    public String pageUrlPrefix(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(currentPath(request)).append('?');
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if ("page".equals(entry.getKey()) || "_csrf".equals(entry.getKey())) {
                continue;
            }
            for (String value : entry.getValue()) {
                sb.append(UriUtils.encodeQueryParam(entry.getKey(), "UTF-8")).append('=')
                  .append(UriUtils.encodeQueryParam(value, "UTF-8")).append('&');
            }
        }
        return sb.append("page=").toString();
    }
}
