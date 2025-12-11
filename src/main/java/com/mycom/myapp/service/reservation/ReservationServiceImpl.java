package com.mycom.myapp.service.reservation;

import com.mycom.myapp.domain.*;
import com.mycom.myapp.exception.ReservationException;
import com.mycom.myapp.repository.ReservationRepository;
import com.mycom.myapp.repository.RoomRepository;
import com.mycom.myapp.repository.TimeSlotRepository;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationValidator validator;

    /**
     * 예약 생성
     * - TimeSlot 기반 검증
     * - 동일 slotId에 다른 CONFIRMED 예약이 있으면 불가
     * - 룸 이용시간(maxUsageMinutes) 체크
     * - status = CONFIRMED로 즉시 확정
     */
    @Override
    @Transactional
    public Reservation createReservation(Long userId, CreateReservationRequest request) {

        // 1. 룸 확인
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ReservationException("존재하지 않는 룸입니다."));

        // 2. 타임슬롯 확인
        TimeSlot timeSlot = timeSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ReservationException("존재하지 않는 타임슬롯입니다."));

        if (!timeSlot.getRoomId().equals(room.getRoomId())) {
            throw new ReservationException("타임슬롯이 해당 룸에 속해있지 않습니다.");
        }

        if (!timeSlot.getIsAvailable()) {
            throw new ReservationException("현재 예약할 수 없는 타임슬롯입니다.");
        }

        // 3. 타임슬롯 자체의 시간 범위 검증 (start < end)
        validator.validateTimeSlotRange(timeSlot);

        // 4. 과거 시간 예약 금지
        validator.validateNotPastSlot(timeSlot);

        // 5. 최대 이용시간 검증
        validator.validateMaxUsage(timeSlot, room);

        // 6. 동일 slotId + CONFIRMED 예약 존재 여부 확인
        long count = reservationRepository.countBySlotIdAndStatus(
                request.getSlotId(),
                ReservationStatus.CONFIRMED
        );

        if (count > 0) {
            throw new ReservationException("해당 타임슬롯에는 이미 예약이 존재합니다.");
        }

        // 7. 예약 생성 (즉시 확정)
        Reservation reservation = Reservation.builder()
                .userId(userId)
                .roomId(room.getRoomId())
                .slotId(timeSlot.getSlotId())
                .status(ReservationStatus.CONFIRMED)
                .reservedAt(LocalDateTime.now())
                .version(0)
                .build();

        return reservationRepository.save(reservation);
    }

    /**
     * 사용자의 모든 예약 조회
     */
    @Override
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    /**
     * 사용자 취소
     * - CONFIRMED 상태만 가능
     * - 시작 30분 전까지 가능
     * - slotId 기준으로 TimeSlot 조회하여 실제 시작시간 확인 후 취소 가능 여부 체크
     */
    @Override
    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException("존재하지 않는 예약입니다."));

        // 본인 예약인지 확인
        if (!reservation.getUserId().equals(userId)) {
            throw new ReservationException("다른 사용자의 예약을 취소할 수 없습니다.");
        }

        // CONFIRMED만 취소 가능
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ReservationException("이미 취소되었거나 거절된 예약은 다시 취소할 수 없습니다.");
        }

        // 예약과 연결된 TimeSlot 조회
        TimeSlot timeSlot = timeSlotRepository.findById(reservation.getSlotId())
                .orElseThrow(() -> new ReservationException("예약에 연결된 타임슬롯이 존재하지 않습니다."));

        // 타임슬롯 기준 30분 전 취소 가능 여부 검증
        validator.validateStartTimeForCancel(timeSlot);

        // 상태 변경
        reservation.setStatus(ReservationStatus.CANCELED);
    }
}
