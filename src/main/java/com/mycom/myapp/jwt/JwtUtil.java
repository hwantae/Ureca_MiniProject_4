package com.mycom.myapp.jwt;

import com.mycom.myapp.config.MyUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
@Getter
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {
    private final MyUserDetailsService myUserDetailsService;

    @Value("${myapp.jwt.secret}")
    private String secretKeyStr;
    private SecretKey secretKey;

    private final long tokenValidDuration = 1000L * 60 * 60;

    @PostConstruct
    protected void init() {
        System.out.println(secretKeyStr);

        secretKey = new SecretKeySpec(
                secretKeyStr.getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().getAlgorithm()
        );
        System.out.println(secretKey);
    }

    public String createToken(String username, List<String> roles) {

        Date now = new Date();

        String token = Jwts.builder()
                .subject(username)   // subject 에 username
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + tokenValidDuration))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        return token;
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {

        UserDetails userDetails = myUserDetailsService.loadUserByUsername(this.getUsernameFromToken(token));
        // principal로 userDetails 객체 자체를 전달해야 @AuthenticationPrincipal이 MyUserDetails를 주입받을 수 있음
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsernameFromToken(String token) {

        String subject = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token).getPayload()
                .getSubject();

        return subject;
    }

    public String getTokenFromHeader(HttpServletRequest request) {
        return request.getHeader("X-AUTH-TOKEN");
    }

    public Claims validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();


            if (claims.getExpiration() != null && claims.getExpiration().before(new Date())) {
                return null; // 만료된 토큰
            }

            return claims;

        } catch (Exception e) {
            return null;
        }
    }

}
