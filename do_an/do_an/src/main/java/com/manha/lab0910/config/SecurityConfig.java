package com.manha.lab0910.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Mật khẩu trong DB lưu dạng BCrypt. Bean này được dùng cả khi
    // kiểm tra lúc đăng nhập lẫn khi tạo tài khoản mới (UC8).
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Trang đăng nhập và file tĩnh: ai cũng vào được
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/lib/**", "/scss/**").permitAll()

                // Phân quyền theo bảng ở mục 3 của đặc tả
                .requestMatchers("/san-pham/**", "/vi-tri-kho/**",
                                 "/phieu-nhap/**", "/phieu-xuat/**").hasRole("THU_KHO")
                .requestMatchers("/bao-cao/**", "/nguoi-dung/**").hasRole("QUAN_LY")

                // Dashboard, tồn kho, cảnh báo: cả hai vai trò, miễn là đã đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")              // trang login tự viết, không dùng trang mặc định
                .loginProcessingUrl("/login")     // form POST về đây, Spring Security tự xử lý
                .usernameParameter("tenDangNhap") // tên field trong form, mặc định là "username"
                .passwordParameter("matKhau")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")             // phải gọi bằng POST (có kèm CSRF token)
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Đăng nhập rồi nhưng vào URL không đúng vai trò -> trang 403
            .exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }
}
