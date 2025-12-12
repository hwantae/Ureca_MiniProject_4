package com.mycom.myapp.config;

import com.mycom.myapp.domain.UserRole;
import com.mycom.myapp.domain.Users;
import com.mycom.myapp.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Users> optionalUsers = userRepository.findByEmail(email);

        if(optionalUsers.isPresent()){
            Users users = optionalUsers.get();
            List<UserRole> listUserRole = users.getUserRoles();

            String roleName = "ROLE_" + users.getUserRoles();
            List<SimpleGrantedAuthority> authorities =
                    listUserRole.stream()
                            .map(UserRole::getName)
                            .map( name -> "ROLE_" + name) // MyUserDetails 부터 "ROLE_" 사용
                            .map(SimpleGrantedAuthority::new)
                            .toList();

            return MyUserDetails.builder()
                    .username(users.getEmail())
                    .password(users.getPassword())
                    //.authorities(authorities)
                    .id(users.getUserId())
                    .name(users.getName())
                    .email(users.getEmail())
                    .build();
        }
        throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
    }
}
