-- =======================================================
-- Flyway Migration V4: Add User Role Tables for Security
-- =======================================================

-- 1. USER_ROLE TABLE (권한 정의 테이블)
-- Spring Security의 GrantedAuthority 역할을 수행
CREATE TABLE user_role (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(50) NOT NULL UNIQUE
);

-- 초기 권한 데이터 삽입 (선택 사항이지만, 일반적으로 필요)
INSERT INTO user_role (name) VALUES ('ROLE_USER');
INSERT INTO user_role (name) VALUES ('ROLE_ADMIN');
-- 'ROLE_' 접두사는 Spring Security의 표준입니다.


-- 2. USER_USER_ROLES TABLE (User와 Role의 매핑 테이블)
-- 기존 user 테이블은 V3에서 users로 변경되었으므로, users 테이블과 연결합니다.
-- 이는 Spring Data JPA에서 @ManyToMany 관계를 나타내는 기본 방식입니다.
CREATE TABLE user_user_roles (
                                 user_id BIGINT NOT NULL,
                                 user_roles_id INT NOT NULL, -- 요청하신 컬럼명 user_roles_id 사용

                                 PRIMARY KEY (user_id, user_roles_id),

    -- users 테이블과의 외래 키 설정
                                 CONSTRAINT fk_uur_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES users(user_id)
                                         ON DELETE CASCADE,

    -- user_role 테이블과의 외래 키 설정
                                 CONSTRAINT fk_uur_role
                                     FOREIGN KEY (user_roles_id)
                                         REFERENCES user_role(id)
                                         ON DELETE CASCADE
);

-- 3. INDEX (성능 최적화)
-- user_roles_id를 기준으로도 조회가 자주 일어날 경우를 대비한 인덱스
CREATE INDEX idx_uur_role
    ON user_user_roles (user_roles_id);