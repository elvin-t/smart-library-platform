package org.mariaelvin.library.borrow_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BorrowServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BorrowServiceApplication.class, args);
	}

}
