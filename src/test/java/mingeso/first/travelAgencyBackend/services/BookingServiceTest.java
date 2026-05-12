package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.BookingEntity;
import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.entities.UserEntity;
import mingeso.first.travelAgencyBackend.enums.BookingStatus;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import mingeso.first.travelAgencyBackend.repositories.BookingRepository;
import mingeso.first.travelAgencyBackend.repositories.TourPackageRepository;
import mingeso.first.travelAgencyBackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    @Mock
    private UserRepository userRepository; // mock agregado

    @InjectMocks
    private BookingService bookingService;

    private UserEntity mockUser;
    private TourPackageEntity mockPackage;
    private BookingEntity mockBooking;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setId(1L);
        mockUser.setName("Juan");

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
        // GIVEN
        mockBooking.setPassengersCount(5);
        when(packageRepository.findById(1L)).thenReturn(Optional.of(mockPackage));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser)); // INDISPENSABLE
        when(bookingRepository.countByUserAndStateBooking(mockUser, BookingStatus.CONFIRMED)).thenReturn(3L);
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        BookingEntity result = bookingService.createBooking(mockBooking);

        // THEN:
        assertThat(result.getTotalAmount()).isEqualByComparingTo("400.00");
    }

    @Test
    @DisplayName("Debe aplicar el tope máximo de descuento del 20%")
    void createBooking_ShouldApplyMaxDiscountTope() {
        // GIVEN
        mockBooking.setPassengersCount(10);
        when(packageRepository.findById(1L)).thenReturn(Optional.of(mockPackage));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(bookingRepository.countByUserAndStateBooking(mockUser, BookingStatus.CONFIRMED)).thenReturn(10L);
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        BookingEntity result = bookingService.createBooking(mockBooking);

        // THEN
        assertThat(result.getTotalAmount()).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay suficientes cupos")
    void createBooking_InsufficientSlots() {
        // GIVEN
        mockBooking.setPassengersCount(11);
        when(packageRepository.findById(1L)).thenReturn(Optional.of(mockPackage));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // WHEN & THEN
        assertThatThrownBy(() -> bookingService.createBooking(mockBooking))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Not enough available slots");
    }
    @Test
    @DisplayName("Debe retornar lista de reservas cuando el usuario tiene historial")
    void shouldReturnBookingsWhenUserHasHistory() {
        // Given
        Long userId = 1L;
        UserEntity user = new UserEntity();
        user.setId(userId);

        BookingEntity booking1 = new BookingEntity();
        booking1.setId(101L);
        booking1.setUser(user);

        BookingEntity booking2 = new BookingEntity();
        booking2.setId(102L);
        booking2.setUser(user);

        when(bookingRepository.findByUserId(userId)).thenReturn(Arrays.asList(booking1, booking2));

        // When
        List<BookingEntity> result = bookingService.getBookingsByUserId(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUser().getId()).isEqualTo(userId);
        verify(bookingRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Debe retornar la reserva cuando el ID existe")
    void shouldReturnBookingWhenIdExists() {
        // Given
        Long bookingId = 101L;
        BookingEntity booking = new BookingEntity();
        booking.setId(bookingId);
        booking.setStateBooking(BookingStatus.CONFIRMED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // When
        BookingEntity result = bookingService.getBookingById(bookingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookingId);
        assertThat(result.getStateBooking()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la reserva no existe")
    void shouldThrowExceptionWhenBookingDoesNotExist() {
        // Given
        Long bookingId = 999L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Reserva no encontrada");
    }
}
