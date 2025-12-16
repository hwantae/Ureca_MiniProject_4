package com.mycom.myapp.test;

import com.mycom.myapp.domain.UserRole;
import com.mycom.myapp.domain.Users;
import com.mycom.myapp.users.dto.UserDto;
import com.mycom.myapp.users.dto.UserResultDto;
import com.mycom.myapp.users.repository.UserRepository;
import com.mycom.myapp.users.repository.UserRoleRepository;
import com.mycom.myapp.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class RegisterTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserService userService;

    // UserServiceImpl 로직상 DB에 "ROLE_USER"가 반드시 있어야 하므로 미리 셋팅
    @BeforeEach
    public void initData() {
        if (userRoleRepository.findByName("ROLE_USER") == null) {
            UserRole role = new UserRole();
            role.setName("ROLE_USER");
            userRoleRepository.save(role);
        }
    }

    // #1. Repository 테스트
    @Test
    @Transactional
    public void testRegisterRepository() {
        // Given
        Users user = Users.builder()
                .name("이길동")
                .email("repository@test.com")
                .password("1234")
                .phonenumber("010-1111-1111")
                .build();

        // When
        Users savedUser = userRepository.save(user);

        // Then
        assertNotNull(savedUser);
        assertEquals("이길동", savedUser.getName());
    }

    // #2. Service 테스트
    @Test
    @Transactional
    public void testRegisterService() {
        // Given (Service는 UserDto를 받도록 정의되어 있음)
        UserDto userDto = UserDto.builder()
                .name("김철수")
                .email("service@test.com")
                .password("5678")
                .phoneNumber("010-2222-2222")
                .build();

        // When
        UserResultDto result = userService.insertUser(userDto);

        // Then
        // 1. 결과 DTO 확인
        assertEquals("success", result.getResult());

        // 2. 실제 DB 저장 확인 (Rollback 되더라도 이 트랜잭션 안에서는 조회 가능)
        Users dbUser = userRepository.findByEmail("service@test.com").orElse(null);
        assertNotNull(dbUser);
        assertEquals("김철수", dbUser.getName());
    }
}