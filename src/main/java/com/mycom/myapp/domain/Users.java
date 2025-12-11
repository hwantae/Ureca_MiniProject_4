package com.mycom.myapp.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(unique = true, length = 20)
    private String phonenumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_user_roles",       // V4 SQL의 테이블명
            joinColumns = @JoinColumn(
                    name = "user_id"            // V4 SQL의 유저 FK 컬럼명 (여기가 핵심!)
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "user_roles_id"      // V4 SQL의 권한 FK 컬럼명
            )
    )
    private List<UserRole> userRoles;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
