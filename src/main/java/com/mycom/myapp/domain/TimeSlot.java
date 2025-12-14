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
@Data
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
}
