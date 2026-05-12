package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.dto.ReportResponseDTO;
import mingeso.first.travelAgencyBackend.repositories.BookingRepository;
import mingeso.first.travelAgencyBackend.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("Debe retornar estadísticas correctas cuando hay datos")
    void getAdminDashboardStats_ShouldReturnCorrectStats() {
        // GIVEN
        when(bookingRepository.calculateTotalRevenue()).thenReturn(500000L);
        when(bookingRepository.countByStatus("CONFIRMED")).thenReturn(10L);
        when(userRepository.count()).thenReturn(5L);
        when(bookingRepository.findTopSellingPackages(any(PageRequest.class)))
                .thenReturn(List.of("Torres del Paine"));

        // WHEN
        ReportResponseDTO result = reportService.getAdminDashboardStats();

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTotalRevenue()).isEqualTo(500000L);
        assertThat(result.getTotalBookings()).isEqualTo(10L);
        assertThat(result.getActiveUsers()).isEqualTo(5L);
        assertThat(result.getTopPackageName()).isEqualTo("Torres del Paine");
    }

    @Test
    @DisplayName("Debe manejar correctamente el caso cuando no hay ventas")
    void getAdminDashboardStats_EmptyData_ShouldReturnDefaultMessage() {
        // GIVEN
        when(bookingRepository.calculateTotalRevenue()).thenReturn(0L);
        when(bookingRepository.countByStatus("CONFIRMED")).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(bookingRepository.findTopSellingPackages(any(PageRequest.class)))
                .thenReturn(List.of());

        // WHEN
        ReportResponseDTO result = reportService.getAdminDashboardStats();

        // THEN
        assertThat(result.getTopPackageName()).isEqualTo("Sin ventas aún");
        assertThat(result.getTotalRevenue()).isZero();
    }
}