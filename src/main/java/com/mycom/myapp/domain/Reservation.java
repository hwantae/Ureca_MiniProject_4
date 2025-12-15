package com.mycom.myapp.domain;

import com.mycom.myapp.exception.ReservationException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE) // 외부에서는 create(...) 정적 팩터리 사용
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long slotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private LocalDateTime reservedAt; // 예약 생성 시각

    @Column(nullable = false)
    private LocalDate reservationDate; // 실제 예약 날짜 (V6 추가)

    @Version
    @Column(nullable = false)
    private Integer version;

    /**
     * 예약 생성용 정적 팩토리 메서드.
     * - 항상 CONFIRMED 상태로 시작
     * - 예약 생성 시간을 지금으로 설정
     */
    public static Reservation create(Long userId, Room room, TimeSlot timeSlot, java.time.LocalDate reservationDate) {
        return Reservation.builder()
                .userId(userId)
                .roomId(room.getRoomId())
                .slotId(timeSlot.getSlotId())
                .reservationDate(reservationDate)
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.now())
                .version(0)
                .build();
    }

    // ==========================
    // 상태/소유자 관련 도메인 로직
    // ==========================

    /** 이 예약의 소유자인지 확인 (boolean) */
    public boolean isOwner(Long userId) {
        return this.userId.equals(userId);
    }

    /** 소유자가 아니면 예외 */
    public void validateOwner(Long userId) {
        if (!isOwner(userId)) {
            throw new ReservationException("다른 사용자의 예약을 취소할 수 없습니다.");
        }
    }

    /** CONFIRMED 상태인지 여부 */
    public boolean isConfirmed() {
        return this.status == ReservationStatus.CONFIRMED;
    }

    /**
     * 취소 가능 상태인지 검증 후 상태 변경.
     * (시간 관련 제약은 Service/Validator에서 검사하고,
     *  Reservation은 "상태 전이 규칙"만 책임진다.)
     */
    public void cancel() {
        if (!isConfirmed()) {
            throw new ReservationException("이미 취소되었거나 거절된 예약입니다.");
        }
        this.status = ReservationStatus.CANCELED;
    }

    /** 추후 ADMIN 기능 확장용 (지금 바로 쓰지 않아도 됨) */
    public void reject() {
        if (!isConfirmed()) {
            throw new ReservationException("확정된 예약만 거절할 수 있습니다.");
        }
        this.status = ReservationStatus.REJECTED;
    }
}
