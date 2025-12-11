package com.mycom.myapp.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token  = jwtUtil.getTokenFromHeader(request);

        Claims claims = null;
        if (token != null) {
            claims = jwtUtil.validateToken(token); // 유효하면 Claims, 아니면 null
        }

        System.out.println(claims);

        if (claims != null) {

            // 우리의 토큰 검증 로직은 여기까지
            System.out.println("토큰 검증 완료");

            // Spring Security 가 Client request 에 대한 기본 검증을 이어가도록 처리
            // 토큰으로부터 username, roles 을 얻어서 이후 filter chaing 을 이어 나가야 함.
            String username = claims.getSubject(); // token 생성시 subject 에 userId 저장

            System.out.println("username");
            System.out.println(username);

            List<?> roles = (List<?>) claims.get("roles"); // List<String> 으로 token 에 넣었지만 꺼낼 때는 ? 으로 우선 List 를 만든다.

            // String role 을 SimpleGrantedAuthority 로
            // 아래 UsernamePasswordAuthenticationToken 객체 생성자에 전달.
            //
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(roleName -> (String) roleName)  // <?> -> <String>
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            System.out.println("authorities");
            System.out.println(authorities);

            // 아래 2개 비교할 때 console 의 로그 확인
            // #1. 1차 검증만 진행
//        	UsernamePasswordAuthenticationToken authenticationToken
//        			= new UsernamePasswordAuthenticationToken(username, null, authorities);


            // #2. 2차 검증 ( DB Access 추가 ) 까지 한다면
            //     사용자 요청마다 DB Access 필요
            UsernamePasswordAuthenticationToken authenticationToken = jwtUtil.getAuthentication(token);

            // 현재 Filter 를 처리하는 Thread 의 Security Context 에 저장해서 이어지는 request lifecycle 에서 authenticationToken 이 유효하도록 한다.
            // 아래 코드가 없으면 security context 가 empty 가 되어 이어지는 request lifecycle 에 대해 unauthenticated 로 간주
            // 401(Unauthorized), 403(Forbidden)
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        }

        filterChain.doFilter(request, response);
    }
}
