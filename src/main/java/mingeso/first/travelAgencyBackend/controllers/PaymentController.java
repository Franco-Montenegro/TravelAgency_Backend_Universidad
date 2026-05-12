package mingeso.first.travelAgencyBackend.controllers;

import mingeso.first.travelAgencyBackend.entities.PaymentEntity;
import mingeso.first.travelAgencyBackend.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentEntity> processPayment(@RequestBody PaymentEntity payment) {
        PaymentEntity processedPayment = paymentService.processPayment(payment);
        return new ResponseEntity<>(processedPayment, HttpStatus.CREATED);
    }

}