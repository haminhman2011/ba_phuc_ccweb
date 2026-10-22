package com.dangkymonhoc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.dangkymonhoc.security.CustomUserDetailsService;
import com.dangkymonhoc.security.RoleBasedSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomUserDetailsService userDetailsService)
            throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/favicon.ico",
                                 "/error", "/error/**", "/403").permitAll()
                .requestMatchers("/admin/**", "/api/**").hasRole("ADMIN")
                .requestMatchers("/gv/**").hasRole("GV")
                .requestMatchers("/sv/**").hasRole("SV")
                // "/", "/doi-mat-khau": chỉ cần đã đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(new RoleBasedSuccessHandler())
                // QĐ9: tài khoản bị khóa có thông báo riêng
                .failureHandler((request, response, ex) -> response.sendRedirect(request.getContextPath()
                        + (ex instanceof DisabledException ? "/login?locked" : "/login?error")))
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("dang-ky-mon-hoc-remember-me")
                .tokenValiditySeconds(7 * 24 * 60 * 60)
                .userDetailsService(userDetailsService)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }
}
