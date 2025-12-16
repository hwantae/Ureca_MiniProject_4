package com.mycom.myapp.service.reservation;

import com.mycom.myapp.domain.*;
import com.mycom.myapp.exception.ReservationException;
import com.mycom.myapp.repository.ReservationRepository;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.RoomTimeBlockRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final RoomTimeBlockRepository roomTimeBlockRepository;
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

        if (!timeSlot.getRoomId().equals(room.getRoomId())) {
            throw new ReservationException("타임슬롯이 해당 룸에 속해있지 않습니다.");
        }

        // TODO: V5 적용 후 B담당 수정 필요 - isAvailable 필드 제거됨
        // if (!timeSlot.getIsAvailable()) {
        //     throw new ReservationException("현재 예약할 수 없는 타임슬롯입니다.");
        // }

        // 3. 예약 날짜 추출 (startTime에서 날짜만)
        java.time.LocalDate targetDate = request.getStartTime().toLocalDate();

        // 4. 시간 관련 비즈니스 룰 검증
        validator.validateTimeSlotRange(timeSlot);
        validator.validateNotPastSlot(timeSlot, targetDate);
        validator.validateMaxUsage(timeSlot, room);

        // ✅ 4-1. 관리자 차단(block) 검증 (C담당 기능이지만, 예약 생성 시 필수 체크)
        boolean blocked = roomTimeBlockRepository.existsByRoomIdAndBlockDateAndStartTime(
                room.getRoomId(),
                targetDate,
                timeSlot.getStartTime()
        );
        if (blocked) {
            throw new ReservationException("해당 시간은 관리자에 의해 차단되었습니다.");
        }

        // 5. 이미 CONFIRMED 예약이 있는지 방어적 체크
        ensureSlotNotAlreadyReserved(timeSlot.getSlotId(), targetDate);

        try {
            // 6. 타임슬롯 사용 가능 여부 다시 한 번 체크
            // [V5 호환성 수정] TimeSlot은 템플릿이므로 상태(isAvailable)를 가지지 않음 -> 로직 주석 처리
            // if (!Boolean.TRUE.equals(timeSlot.getIsAvailable())) {
            //    throw new ReservationException("현재 예약할 수 없는 타임슬롯입니다.");
            // }

            // 7. 이 타임슬롯을 이제 점유하겠다고 표시
            // [V5 호환성 수정] TimeSlot은 공유 템플릿이므로 상태 변경 불가 -> 로직 주석 처리
            // timeSlot.setIsAvailable(false);
            // timeSlotRepository.save(timeSlot);

            // 8. Reservation 도메인 객체 생성 (즉시 확정)
            // ✅ 승인/반려 없음 → create()에서 CONFIRMED로 생성
            Reservation reservation = Reservation.create(userId, room, timeSlot, targetDate);

            // 9. 저장 (DB 유니크 제약조건에 의해 중복 시 예외 발생)
            return reservationRepository.save(reservation);

        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            throw new ReservationException("다른 사용자가 먼저 해당 타임슬롯을 예약했습니다. 다시 시도해주세요.");
        } catch (DataIntegrityViolationException e) {
            throw new ReservationException("이미 해당 타임슬롯에 대한 예약이 존재합니다.");
        }
    }

    // ==========================
    // 예약 조회
    // ==========================
    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(Long userId) {
        // return reservationRepository.findByUserId(userId);

        // ✅ 정렬이 필요하면 아래 메서드 추가 후 사용
        return reservationRepository.findByUserIdOrderByReservationDateDesc(userId);
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
        validator.validateStartTimeForCancel(timeSlot, reservation.getReservationDate());

        // 5. 상태 전이 (도메인 메서드)
        reservation.cancel();

        // ✅ [중요] V5의 UNIQUE(room_id, slot_id, reservation_date) 때문에
        //    CANCELED로만 바꾸면 같은 슬롯/날짜로 재예약이 불가능해짐.
        //    최종 단계에서는 "취소 = 삭제"로 처리해서 재예약 가능하도록 한다.
        reservationRepository.delete(reservation);
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
        // [V5 호환성 수정] TimeSlot은 상태 없음
        // if (!Boolean.TRUE.equals(timeSlot.getIsAvailable())) {
        //     throw new ReservationException("현재 예약할 수 없는 타임슬롯입니다.");
        // }
    }

    /** 같은 slotId, 같은 날짜에 이미 CONFIRMED 예약이 있는지 확인 */
    private void ensureSlotNotAlreadyReserved(Long slotId, java.time.LocalDate targetDate) {
        long count = reservationRepository.countBySlotIdAndReservationDateAndStatus(
                slotId,
                targetDate,
                ReservationStatus.CONFIRMED
        );
        if (count > 0) {
            throw new ReservationException("해당 타임슬롯에는 이미 예약이 존재합니다.");
        }
    }

    @Override
    public List<Long> getReservedSlotIds(Long roomId, LocalDate date) {
        // 해당 roomId + date에 대해 확정(CONFIRMED)된 예약의 slotId만 추출
        return reservationRepository
                .findByRoomIdAndReservationDateAndStatus(roomId, date, ReservationStatus.CONFIRMED)
                .stream()
                .map(Reservation::getSlotId)
                .distinct()
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getRoomReservations(Long roomId, LocalDate date) {
        return reservationRepository.findByRoomIdAndReservationDateAndStatus(
                roomId, date, ReservationStatus.CONFIRMED
        );
    }
}
