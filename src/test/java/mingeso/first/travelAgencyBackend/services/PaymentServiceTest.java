package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.BookingEntity;
import mingeso.first.travelAgencyBackend.entities.PaymentEntity;
import mingeso.first.travelAgencyBackend.enums.BookingStatus;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import mingeso.first.travelAgencyBackend.repositories.BookingRepository;
import mingeso.first.travelAgencyBackend.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    private BookingEntity mockBooking;
    private PaymentEntity mockPayment;

    @BeforeEach
    void setUp() {
        mockBooking = new BookingEntity();
        mockBooking.setId(1L);
        mockBooking.setTotalAmount(new BigDecimal("500.00"));
        mockBooking.setStateBooking(BookingStatus.PENDING_PAYMENT);

        mockPayment = new PaymentEntity();
        mockPayment.setBooking(mockBooking);
        mockPayment.setAmount(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Debe procesar el pago exitosamente y cambiar estado a CONFIRMED")
    void processPaymentSuccess() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));
        when(paymentRepository.existsByBookingId(1L)).thenReturn(false);
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        PaymentEntity result = paymentService.processPayment(mockPayment);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isNotNull();
        assertThat(mockBooking.getStateBooking()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository).save(mockBooking);
        verify(paymentRepository).save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la reserva ya fue pagada")
    void processPaymentAlreadyPaid() {
        // Arrange
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));
        when(paymentRepository.existsByBookingId(1L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(mockPayment))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("This booking has already been paid.");
    }

    @Test
    @DisplayName("Debe lanzar excepción si la reserva está cancelada")
    void processPaymentCancelledBooking() {
        // Arrange
        mockBooking.setStateBooking(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(mockBooking));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(mockPayment))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot pay for a cancelled or expired booking.");
    }
}