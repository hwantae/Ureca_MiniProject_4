package com.mycom.myapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class UrecaMiniProject4Application {

	public static void main(String[] args) {
		SpringApplication.run(UrecaMiniProject4Application.class, args);
	}

}
