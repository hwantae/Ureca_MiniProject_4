package com.mycom.myapp.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="room")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue
    private Long roomId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false)
    private Integer capacity;
    @Column(nullable = false)
    private Boolean isAvailable;
    @Column(nullable = false)
    private Integer maxUsageMinutes;
}
