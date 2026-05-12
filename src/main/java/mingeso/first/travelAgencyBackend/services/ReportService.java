package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.dto.ReportResponseDTO;
import mingeso.first.travelAgencyBackend.enums.BookingStatus;
import mingeso.first.travelAgencyBackend.repositories.BookingRepository;
import mingeso.first.travelAgencyBackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    public ReportResponseDTO getAdminDashboardStats() {
        List<String> topPackages = bookingRepository.findTopSellingPackages(PageRequest.of(0, 1));
        String bestSeller = topPackages.isEmpty() ? "Sin ventas aún" : topPackages.get(0);

        return ReportResponseDTO.builder()
                .totalRevenue(bookingRepository.calculateTotalRevenue())
                .totalBookings(bookingRepository.countByStateBooking(BookingStatus.CONFIRMED))
                .topPackageName(bestSeller)
                .activeUsers(userRepository.count())
                .build();
    }
}