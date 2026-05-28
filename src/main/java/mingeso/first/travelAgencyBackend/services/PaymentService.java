package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.BookingEntity;
import mingeso.first.travelAgencyBackend.entities.PaymentEntity;
import mingeso.first.travelAgencyBackend.enums.BookingStatus;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import mingeso.first.travelAgencyBackend.repositories.BookingRepository;
import mingeso.first.travelAgencyBackend.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public PaymentEntity processPayment(PaymentEntity payment) {

        BookingEntity booking = bookingRepository.findById(payment.getBooking().getId())
                .orElseThrow(() -> new BadRequestException("Booking not found."));

        if (paymentRepository.existsByBookingId(booking.getId()) || booking.getStateBooking() == BookingStatus.CONFIRMED) {
            throw new BadRequestException("This booking has already been paid.");
        }

        if (booking.getStateBooking() == BookingStatus.CANCELLED || booking.getStateBooking() == BookingStatus.EXPIRED) {
            throw new BadRequestException("Cannot pay for a cancelled or expired booking.");
        }

        payment.setAmount(booking.getTotalAmount());
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setBooking(booking);

        booking.setStateBooking(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        return paymentRepository.save(payment);
    }
}