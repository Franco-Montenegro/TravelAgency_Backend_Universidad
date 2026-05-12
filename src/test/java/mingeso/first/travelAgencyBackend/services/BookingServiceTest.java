package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.BookingEntity;
import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.entities.UserEntity;
import mingeso.first.travelAgencyBackend.enums.BookingStatus;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import mingeso.first.travelAgencyBackend.repositories.BookingRepository;
import mingeso.first.travelAgencyBackend.repositories.TourPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TourPackageRepository packageRepository;

    @InjectMocks
    private BookingService bookingService;

    private UserEntity mockUser;
    private TourPackageEntity mockPackage;
    private BookingEntity mockBooking;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setId(1L);

        mockPackage = new TourPackageEntity();
        mockPackage.setId(1L);
        mockPackage.setPrice(new BigDecimal("100.00"));
        mockPackage.setAvailableSlots(10);

        mockBooking = new BookingEntity();
        mockBooking.setUser(mockUser);
        mockBooking.setTourPackage(mockPackage);
        mockBooking.setPassengersCount(2);
    }

    @Test
    @DisplayName("Debe crear una reserva exitosamente con descuento acumulado")
    void createBooking_SuccessWithDiscounts() {
        mockBooking.setPassengersCount(5);
        when(packageRepository.findById(1L)).thenReturn(Optional.of(mockPackage));
        when(bookingRepository.countByUserAndStateBooking(mockUser, BookingStatus.CONFIRMED)).thenReturn(3L);
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        BookingEntity result = bookingService.createBooking(mockBooking);

        // THEN
        assertThat(result.getTotalAmount()).isEqualByComparingTo("400.00");
        assertThat(result.getTotalDiscount()).isEqualByComparingTo("100.00");
        assertThat(mockPackage.getAvailableSlots()).isEqualTo(5);
        verify(packageRepository).save(mockPackage);
    }

    @Test
    @DisplayName("Debe aplicar el tope máximo de descuento del 20%")
    void createBooking_ShouldApplyMaxDiscountTope() {
        // GIVEN
        mockBooking.setPassengersCount(10);
        when(packageRepository.findById(1L)).thenReturn(Optional.of(mockPackage));

        when(bookingRepository.countByUserAndStateBooking(mockUser, BookingStatus.CONFIRMED)).thenReturn(10L);
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        BookingEntity result = bookingService.createBooking(mockBooking);

        // THEN
        assertThat(result.getTotalAmount()).isEqualByComparingTo("800.00");
        assertThat(result.getTotalDiscount()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay suficientes cupos")
    void createBooking_InsufficientSlots() {
        mockBooking.setPassengersCount(11);
        when(packageRepository.findById(1L)).thenReturn(Optional.of(mockPackage));

        assertThatThrownBy(() -> bookingService.createBooking(mockBooking))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Not enough available slots");
    }
}