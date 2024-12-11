package site.campingon.campingon.reservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.campingon.campingon.camp.entity.Camp;
import site.campingon.campingon.camp.entity.CampSite;
import site.campingon.campingon.camp.entity.Induty;
import site.campingon.campingon.reservation.entity.Reservation;
import site.campingon.campingon.reservation.entity.ReservationStatus;
import site.campingon.campingon.reservation.repository.ReservationRepository;
import site.campingon.campingon.user.entity.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationStatusUpdateTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationStatusUpdate reservationStatusUpdate;

    @Mock
    private Reservation reservation1;
    @Mock
    private Reservation reservation2;

    @BeforeEach
    void setUp() {
        when(reservation1.getStatus()).thenReturn(ReservationStatus.RESERVED);
        when(reservation2.getStatus()).thenReturn(ReservationStatus.NOTCANCELABLE);
    }

    @Test
    @DisplayName("갱신성공 - 예약불가상태로 갱신")
    void testUpdateStatusToNotCancelable() {

        // Given - 밀리초 제거 필요
        LocalDateTime targetTime = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0).withSecond(0).withNano(0);
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        when(reservationRepository.findByCheckin(targetTime)).thenReturn(reservations);

        // 밀리초를 제거한 targetTime으로 설정
        when(reservationRepository.findByCheckin(targetTime)).thenReturn(reservations);

        // When
        reservationStatusUpdate.updateStatusToNotCancelable();

        // Then
        ArgumentCaptor<List<Reservation>> captor = ArgumentCaptor.forClass(List.class);
        verify(reservationRepository, times(1)).saveAll(captor.capture());

        List<Reservation> savedReservations = captor.getValue();
        assertEquals(2, savedReservations.size());
        verify(reservation1).changeStatus(ReservationStatus.NOTCANCELABLE);
        verify(reservation2, never()).changeStatus(ReservationStatus.NOTCANCELABLE);
    }

    @Test
    @DisplayName("갱신성공 - 체크인완료상태로 갱신")
    void testUpdateStatusToCompleted() {

        // Given - 밀리초 제거 필요
        LocalDateTime targetTime = LocalDateTime.now().withHour(15).withMinute(0).withSecond(0).withNano(0);

        // 상태를 변경할 수 있도록 설정
        when(reservation1.getStatus()).thenReturn(ReservationStatus.RESERVED);
        when(reservation2.getStatus()).thenReturn(ReservationStatus.COMPLETED);

        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        // 밀리초를 제거한 targetTime으로 설정
        when(reservationRepository.findByCheckin(targetTime)).thenReturn(reservations);

        // When
        reservationStatusUpdate.updateStatusToCompleted();

        // Then
        ArgumentCaptor<List<Reservation>> captor = ArgumentCaptor.forClass(List.class);
        verify(reservationRepository, times(1)).saveAll(captor.capture());

        List<Reservation> savedReservations = captor.getValue();
        assertEquals(2, savedReservations.size());
        verify(reservation1).changeStatus(ReservationStatus.COMPLETED);
        verify(reservation2, never()).changeStatus(ReservationStatus.COMPLETED);
    }
}