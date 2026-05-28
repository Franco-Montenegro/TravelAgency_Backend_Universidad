package mingeso.first.travelAgencyBackend.controllers;

import mingeso.first.travelAgencyBackend.entities.PaymentEntity;
import mingeso.first.travelAgencyBackend.entities.BookingEntity;
import mingeso.first.travelAgencyBackend.services.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentEntity> processPayment(
            @PathVariable Long bookingId,
            @RequestBody PaymentEntity payment) {

        BookingEntity bookingRef = new BookingEntity();
        bookingRef.setId(bookingId);
        payment.setBooking(bookingRef);

        PaymentEntity processedPayment = paymentService.processPayment(payment);
        return new ResponseEntity<>(processedPayment, HttpStatus.CREATED);
    }
}