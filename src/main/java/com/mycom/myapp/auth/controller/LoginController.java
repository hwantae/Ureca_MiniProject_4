package com.mycom.myapp.auth.controller;

import com.mycom.myapp.auth.dto.LoginResultDto;
import com.mycom.myapp.auth.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    @PostMapping("login")
    public LoginResultDto login(@RequestParam("username") String username, @RequestParam("password") String password){
        LoginResultDto loginResultDto = loginService.login(username, password);

        return loginResultDto;
    }
}
