package com.mycom.myapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mycom.myapp.config.MyUserDetails;
import com.mycom.myapp.domain.Reservation;
import com.mycom.myapp.service.reservation.ReservationService;
import com.mycom.myapp.service.reservation.dto.CreateReservationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ✅ Spring Security/JWT 필터 영향 없이
 * - @AuthenticationPrincipal만 정상 주입되도록 Resolver를 직접 등록
 */
@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock ReservationService reservationService;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ReservationController controller = new ReservationController(reservationService);

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                // ✅ @AuthenticationPrincipal 처리용 Resolver 직접 등록
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private UsernamePasswordAuthenticationToken authWithUserId(long userId) {
        MyUserDetails principal = mock(MyUserDetails.class);
        when(principal.getId()).thenReturn(userId);
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    @Test
    @DisplayName("POST /api/reservations - service.createReservation(userId, request) 호출")
    void createReservation_callsService() throws Exception {
        long userId = 10L;

        CreateReservationRequest req = new CreateReservationRequest();
        req.setRoomId(1L);
        req.setSlotId(3L);
        req.setStartTime(LocalDateTime.of(2025, 12, 16, 9, 0));
        req.setEndTime(LocalDateTime.of(2025, 12, 16, 10, 0));

        // ReservationResponse.from(created) 내부에서 getter들을 부를 수 있으니 최소 스텁
        Reservation created = mock(Reservation.class);
        when(reservationService.createReservation(eq(userId), any(CreateReservationRequest.class)))
                .thenReturn(created);

        mockMvc.perform(post("/api/reservations")
                        .principal(authWithUserId(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        verify(reservationService, times(1)).createReservation(eq(userId), any(CreateReservationRequest.class));
    }

    @Test
    @DisplayName("GET /api/reservations/me - service.getUserReservations(userId) 호출")
    void getMyReservations_callsService() throws Exception {
        long userId = 7L;
        when(reservationService.getUserReservations(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/reservations/me")
                        .principal(authWithUserId(userId)))
                .andExpect(status().isOk());

        verify(reservationService, times(1)).getUserReservations(userId);
    }

    @Test
    @DisplayName("DELETE /api/reservations/{reservationId} - service.cancelReservation(userId, reservationId) 호출")
    void cancelReservation_callsService() throws Exception {
        long userId = 5L;
        long reservationId = 99L;

        mockMvc.perform(delete("/api/reservations/{reservationId}", reservationId)
                        .principal(authWithUserId(userId)))
                .andExpect(status().isNoContent());

        verify(reservationService, times(1)).cancelReservation(userId, reservationId);
    }
}
