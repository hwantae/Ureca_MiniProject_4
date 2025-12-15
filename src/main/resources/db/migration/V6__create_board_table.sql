-- ==========================================
-- Flyway Migration V6: Create Board Table
-- ==========================================

CREATE TABLE board (
    -- 1. 글번호 (no)
                       board_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- 2. 글제목
                       title VARCHAR(255) NOT NULL,

    -- 3. 글내용 (긴 내용 저장을 위해 TEXT 타입 사용)
                       content TEXT NOT NULL,

    -- 4. 작성자 (이름을 직접 저장하거나, users 테이블과 조인할 수 있음)
    -- 여기서는 요청하신 대로 '작성자(이름)'을 저장하는 구조로 작성했습니다.
                       writer VARCHAR(50) NOT NULL,

    -- 5. 작성일 (기본값으로 현재 시간 자동 입력)
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- (선택 사항) 빠른 조회를 위한 인덱스 추가 (작성일 역순 정렬 등)
CREATE INDEX idx_board_created_at ON board(created_at DESC);