package com.dangkymonhoc.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.dangkymonhoc.controller.Redirects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Vi phạm quy định nghiệp vụ chưa được controller xử lý: quay lại trang trước kèm thông báo lỗi. */
    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        return Redirects.back(request, "/");
    }

    /** Ví dụ: giảng viên mở trang nhập điểm của môn không được phân công. */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return "error/403";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handleOther(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        // Lỗi request của Spring MVC (thiếu tham số, sai kiểu...) giữ nguyên mã trạng thái
        if (ex instanceof ErrorResponse errorResponse) {
            int status = errorResponse.getStatusCode().value();
            response.setStatus(status);
            if (status == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }
            if (status < 500) {
                log.warn("Yêu cầu không hợp lệ {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
                return "error/500";
            }
        }
        log.error("Lỗi không mong muốn khi xử lý {} {}", request.getMethod(), request.getRequestURI(), ex);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return "error/500";
    }
}
