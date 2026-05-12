package mingeso.first.travelAgencyBackend.repositories;

import mingeso.first.travelAgencyBackend.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByBookingId(Long bookingId);

    Optional<PaymentEntity> findByBookingId(Long bookingId);

    Optional<PaymentEntity> findByTransactionId(String transactionId);
}