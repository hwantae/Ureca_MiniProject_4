-- =========================
-- 1. USER TABLE
-- =========================
CREATE TABLE user (
                      user_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                      email VARCHAR(100) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL,
                      name VARCHAR(50) NOT NULL,
                      phone_number VARCHAR(20) UNIQUE,

                      role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',

                      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- 2. ROOM TABLE
-- =========================
CREATE TABLE room (
                      room_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                      name VARCHAR(100) NOT NULL UNIQUE,
                      capacity INT NOT NULL,

                      is_available BOOLEAN NOT NULL DEFAULT TRUE
);

-- =========================
-- 3. TIME_SLOT TABLE (고정 예약 블록)
-- =========================
CREATE TABLE time_slot (
                           slot_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                           room_id BIGINT NOT NULL,

                           slot_date DATE NOT NULL,
                           start_time TIME NOT NULL,
                           end_time TIME NOT NULL,

                           is_available BOOLEAN NOT NULL DEFAULT TRUE,

                           CONSTRAINT fk_slot_room
                               FOREIGN KEY (room_id)
                                   REFERENCES room(room_id)
                                   ON DELETE CASCADE,

                           CONSTRAINT unique_room_slot
                               UNIQUE (room_id, slot_date, start_time, end_time)
);

-- =========================
-- 4. RESERVATION TABLE
-- =========================
CREATE TABLE reservation (
                             reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                             user_id BIGINT NOT NULL,
                             room_id BIGINT NOT NULL,
                             slot_id BIGINT NOT NULL,

                             status ENUM('PENDING','CONFIRMED','CANCELED','REJECTED')
                                            NOT NULL DEFAULT 'PENDING',

                             reserved_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             version INT NOT NULL DEFAULT 0,

                             CONSTRAINT fk_res_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES user(user_id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_res_room
                                 FOREIGN KEY (room_id)
                                     REFERENCES room(room_id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_res_slot
                                 FOREIGN KEY (slot_id)
                                     REFERENCES time_slot(slot_id)
                                     ON DELETE CASCADE,

    -- ✅ 이중 예약 100% 차단
                             CONSTRAINT unique_slot_reservation
                                 UNIQUE (slot_id)
);

-- =========================
-- 5. INDEX (성능 최적화)
-- =========================
CREATE INDEX idx_time_slot_room_date
    ON time_slot(room_id, slot_date);

CREATE INDEX idx_res_user
    ON reservation(user_id);

CREATE INDEX idx_res_room
    ON reservation(room_id);
