package com.dangkymonhoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DangKyMonHocApplication {

	public static void main(String[] args) {
		SpringApplication.run(DangKyMonHocApplication.class, args);
	}

}
