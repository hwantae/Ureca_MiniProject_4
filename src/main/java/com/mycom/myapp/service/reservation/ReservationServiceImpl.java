package com.mycom.myapp.service.reservation;

import com.mycom.myapp.domain.*;
import com.mycom.myapp.exception.ReservationException;
import com.mycom.myapp.repository.ReservationRepository;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationValidator validator;

    // ==========================
    // 예약 생성 (동시성 + 낙관적 락 처리)
    // ==========================
    @Override
    @Transactional
    public Reservation createReservation(Long userId, CreateReservationRequest request) {

        // 1. 룸, 타임슬롯 조회
        Room room = getRoomOrThrow(request.getRoomId());
        TimeSlot timeSlot = getTimeSlotOrThrow(request.getSlotId());

        // 2. 룸/타임슬롯 관계 및 기본 상태 검증
        validateRoomAndSlot(room, timeSlot);

        // 3. 시간 관련 비즈니스 룰 검증
        validator.validateTimeSlotRange(timeSlot);
        validator.validateNotPastSlot(timeSlot);
        validator.validateMaxUsage(timeSlot, room);

        // 4. 이미 CONFIRMED 예약이 있는지 방어적 체크
        ensureSlotNotAlreadyReserved(timeSlot.getSlotId());

        try {
            // 5. 타임슬롯 사용 가능 여부 다시 한 번 체크
            //    (다른 트랜잭션에서 이미 isAvailable=false로 바꾸었을 수 있으니)
            if (!Boolean.TRUE.equals(timeSlot.getIsAvailable())) {
                throw new ReservationException("현재 예약할 수 없는 타임슬롯입니다.");
            }

            // 6. 이 타임슬롯을 이제 점유하겠다고 표시
            //    - @Version이 걸려 있으므로, 동시에 두 트랜잭션이 여기까지 왔을 경우
            //      한 쪽만 성공하고 나머지는 낙관적 락 예외 발생
            timeSlot.setIsAvailable(false);
            timeSlotRepository.save(timeSlot); // 여기서 version 증가 + UPDATE 발생

            // 7. Reservation 도메인 객체 생성 (즉시 확정)
            Reservation reservation = Reservation.create(userId, room, timeSlot);

            // 8. 저장
            return reservationRepository.save(reservation);

        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            // JPA/Hibernate 레벨에서 발생하는 낙관적 락 예외
            throw new ReservationException("다른 사용자가 먼저 해당 타임슬롯을 예약했습니다. 다시 시도해주세요.");
        } catch (DataIntegrityViolationException e) {
            // DB UNIQUE 제약조건 (unique_slot_reservation 등)에 의해 실패하는 경우
            throw new ReservationException("이미 해당 타임슬롯에 대한 예약이 존재합니다.");
        }
    }

    // ==========================
    // 예약 조회
    // ==========================
    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    // ==========================
    // 예약 취소
    // ==========================
    @Override
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {

        // 1. 예약 조회
        Reservation reservation = getReservationOrThrow(reservationId);

        // 2. 소유자 검증 (도메인 메서드)
        reservation.validateOwner(userId);

        // 3. 타임슬롯 조회
        TimeSlot timeSlot = getTimeSlotOrThrow(reservation.getSlotId());

        // 4. 시작 30분 전인지 등 취소 가능 시간 검증
        validator.validateStartTimeForCancel(timeSlot);

        // 5. 상태 전이 (도메인 메서드)
        reservation.cancel();
        // JPA 변경 감지로 UPDATE 쿼리 자동 반영
    }

    // ==========================
    // private 헬퍼 메서드들
    // ==========================

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ReservationException("존재하지 않는 룸입니다."));
    }

    private TimeSlot getTimeSlotOrThrow(Long slotId) {
        return timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ReservationException("존재하지 않는 타임슬롯입니다."));
    }

    private Reservation getReservationOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException("존재하지 않는 예약입니다."));
    }

    /** 룸과 슬롯의 매핑 & 사용 가능 여부 체크 */
    private void validateRoomAndSlot(Room room, TimeSlot timeSlot) {
        if (!timeSlot.getRoomId().equals(room.getRoomId())) {
            throw new ReservationException("타임슬롯이 해당 룸에 속해있지 않습니다.");
        }
        if (!Boolean.TRUE.equals(room.getIsAvailable())) {
            throw new ReservationException("현재 예약할 수 없는 룸입니다.");
        }
        if (!Boolean.TRUE.equals(timeSlot.getIsAvailable())) {
            throw new ReservationException("현재 예약할 수 없는 타임슬롯입니다.");
        }
    }

    /** 같은 slotId에 이미 CONFIRMED 예약이 있는지 확인 */
    private void ensureSlotNotAlreadyReserved(Long slotId) {
        long count = reservationRepository.countBySlotIdAndStatus(
                slotId,
                ReservationStatus.CONFIRMED
        );
        if (count > 0) {
            throw new ReservationException("해당 타임슬롯에는 이미 예약이 존재합니다.");
        }
    }
}
