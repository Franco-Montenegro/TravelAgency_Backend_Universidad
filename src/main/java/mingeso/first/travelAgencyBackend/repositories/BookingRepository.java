package mingeso.first.travelAgencyBackend.repositories;

import mingeso.first.travelAgencyBackend.entities.BookingEntity;
import mingeso.first.travelAgencyBackend.entities.UserEntity;
import mingeso.first.travelAgencyBackend.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    @Query("SELECT COUNT(b) FROM BookingEntity b WHERE b.user = :user AND b.stateBooking = :status")
    long countByUserAndStateBooking(@Param("user") UserEntity user, @Param("status") BookingStatus status);

    List<BookingEntity> findByUser(UserEntity user);
    List<BookingEntity> findByUserId(Long userId);

    @Query("SELECT b FROM BookingEntity b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    List<BookingEntity> findAllByBookingDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<BookingEntity> findByStateBookingAndExpirationDateBefore(BookingStatus status, LocalDate currentDate);
}