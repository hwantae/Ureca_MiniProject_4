package com.mycom.myapp.config;

import com.mycom.myapp.jwt.JwtAuthenticationFilter;
import com.mycom.myapp.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            MyAuthenticationEntryPoint entryPoint
    ) throws Exception {
        http
                // Disable basic HTTP and CSRF
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests((requests) -> requests
                                .requestMatchers(
                                        "/",
                                        "/index.html",
                                        "/.well-known/**",
                                        "/login",
                                        "/login.html",
                                        "/register",
                                        "/register.html", // /login.html 은 필요없는 데, /register.html 은 필요
                                        "/auth/**",
                                        "/users/**",
                                        // board 는 로그인이 필요
                                        "/board.html",	// hyberlink get 요청 허락
//                		"/csrf-token"
                                        // 껍데기만 허락
                                        "/assets/**",
                                        "/css/**",
                                        "/js/**",
                                        "/images/**",
                                        "/favicon.ico",
                                        "/error",
                                        
                                        // C담당 페이지 및 API (Merge 후 추가)
                                        "/rooms.html",
                                        "/room.html",
                                        "/admin_rooms.html",
                                        "/admin_room.html",
                                        "/api/rooms/**",
                                        "/api/slots/**",
                                        "/board_write.html",
                                        "/api/boards/**"
                                ).permitAll()
                                .requestMatchers("/users/**").hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                // 아래 코드 없으면 403 forbidden 응답이 오고 아무런 body 가 없다.
                // formLogin 방식에서 허락되지 않는 요청에 대해 자동으로 login.html 페이지로 분기 (redirect)
                //   또는 MyAuthenticationFailureHandler 로 처리
                // formLogin 을 사용 X => 예외 발생 => json 응답 ( login 필요 ) 하도록 추가 코드 필요.
                .exceptionHandling(
                        exceptionHandlingCustomizer ->
                                exceptionHandlingCustomizer.authenticationEntryPoint(entryPoint)  // <--- This handles unauthorized errors
                )
//            .authenticationProvider(authenticationProvider())
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}
