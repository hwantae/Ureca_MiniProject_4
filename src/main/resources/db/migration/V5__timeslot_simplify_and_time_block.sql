-- ===========================================================
-- V5: TimeSlot 단순화 + RoomTimeBlock 테이블 생성 (C담당)
-- ===========================================================
-- 목적:
-- time_slot을 "하루 단위 템플릿"으로 단순화하고,
-- 날짜/시간 단위의 예약 불가 구간을 room_time_block으로 분리
--
-- 변경사항:
--   1) time_slot에서 slot_date 제거 → 날짜 없이 템플릿만 유지
--   2) room_time_block 테이블 생성 → 특정 날짜/시간 제한 관리
-- ===========================================================


-- ===========================================================  
-- 1) time_slot 단순화: 날짜 개념 제거
-- ===========================================================

-- 인덱스 삭제는 FK 제약으로 불가 - 유지함

-- slot_date 컬럼 제거
ALTER TABLE time_slot DROP COLUMN slot_date;

-- is_available 컬럼 제거 (room_time_block으로 대체)
ALTER TABLE time_slot DROP COLUMN is_available;

-- 3. Reservation 테이블에 날짜 컬럼 추가 및 Unique 제약조건 설정 (동시성 제어용)
ALTER TABLE reservation ADD COLUMN reservation_date DATE NOT NULL;
ALTER TABLE reservation ADD CONSTRAINT unique_reservation_slot UNIQUE (room_id, slot_id, reservation_date);

-- 새 UNIQUE 제약 추가 (방 + 시간대 템플릿)
ALTER TABLE time_slot
    ADD CONSTRAINT unique_room_time_template
        UNIQUE (room_id, start_time, end_time);


-- ===========================================================
-- 2) room_time_block 테이블 생성 (예약 불가 구간)
-- ===========================================================

CREATE TABLE room_time_block (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id    BIGINT NOT NULL,
    block_date DATE   NOT NULL,
    start_time TIME   NOT NULL,
    end_time   TIME   NOT NULL,

    CONSTRAINT uq_room_time_block
        UNIQUE (room_id, block_date, start_time, end_time),

    CONSTRAINT fk_room_time_block_room
        FOREIGN KEY (room_id)
            REFERENCES room(room_id)
            ON DELETE CASCADE
);
