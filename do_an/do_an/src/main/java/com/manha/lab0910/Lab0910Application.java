package com.manha.lab0910;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// Bật cho phép chạy tác vụ định kỳ (@Scheduled), dùng cho job quét cảnh báo hạn dùng
@EnableScheduling
public class Lab0910Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab0910Application.class, args);
    }
}
