package com.mycom.myapp.auth.service;

import com.mycom.myapp.auth.dto.LoginResultDto;
import com.mycom.myapp.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginServiceImpl implements LoginService{
    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;
    @Override
    public LoginResultDto login(String email, String password) {
        LoginResultDto loginResultDto = new LoginResultDto();
        try {
            log.debug("userRepository.findByEmail(email)");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            String username = authentication.getName();
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList();

            String token = jwtUtil.createToken(username, roles);
            System.out.println(token);
            loginResultDto.setResult("success");
            loginResultDto.setToken(token);
        } catch(Exception e){
            loginResultDto.setResult("fail");
        }
        return loginResultDto;
    }
}
