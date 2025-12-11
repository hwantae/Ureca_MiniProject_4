package com.mycom.myapp.users.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

	// SecurityConfig 설정에서 /csrf-token 추가 <= permitAll()
	@GetMapping("/csrf-token")
	public CsrfToken csrf(CsrfToken token) {
		return token;
	}
}
