package com.mycom.myapp.users.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

	@GetMapping("/login")
	public String login() {
		return "/login.html";
	}
	
	@GetMapping("/register")
	public String register() {
		return "/register.html";
	}
	
    // get + 인증 필요 url 은 PageController 에서 제외
//  @GetMapping("/board")
//  public String board() {
//      return "/board.html";
//  }
}
