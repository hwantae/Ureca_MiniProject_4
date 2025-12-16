package com.mycom.myapp.service.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ReservedSlotsResponse {
    private String result;
    private List<Long> slotIds;
}
