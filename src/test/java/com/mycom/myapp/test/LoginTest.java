package com.mycom.myapp.test;

import com.mycom.myapp.auth.controller.LoginController;
import com.mycom.myapp.auth.dto.LoginResultDto;
import com.mycom.myapp.auth.service.LoginService;
import com.mycom.myapp.domain.Users;
import com.mycom.myapp.users.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LoginTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginService loginService;

    @Autowired
    private LoginController loginController;

    @Autowired
    private HttpSession session;

    @Test
    public void testLoginRepository (){
        Optional<Users> optionalUsers = userRepository.findByEmail("a");
        assertTrue(optionalUsers.isPresent());
    }

    @Test
    public void testLoginService(){
        LoginResultDto loginResultDto = loginService.login("a", "a");
        assertNotNull(loginResultDto.getUserDto());
    }

    @Test
    public void testLoginController(){
        LoginResultDto loginResultDto = loginController.login("a", "a");
        assertEquals("success", loginResultDto.getResult());
        assertNotNull(session.getAttribute("loginResult"));
    }
}
