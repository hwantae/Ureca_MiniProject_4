package com.mycom.myapp.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * 영업 시간 템플릿 (V5 구조)
 * - 날짜 없이 시간대만 저장
 * - 모든 날짜에 공통 적용되는 영업시간
 */
@Entity
@Table(name = "time_slot")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slotId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // V5: isAvailable, slotDate 제거됨
    // V5: version 필드는 템플릿 변경 관리를 위해 남겨둘 수 있으나, 예약 동시성 제어용으로는 사용 불가
}
