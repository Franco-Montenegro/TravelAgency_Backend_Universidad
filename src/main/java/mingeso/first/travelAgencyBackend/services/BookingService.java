package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.BookingEntity;
import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.entities.UserEntity;
import mingeso.first.travelAgencyBackend.enums.BookingStatus;

import mingeso.first.travelAgencyBackend.repositories.BookingRepository;
import mingeso.first.travelAgencyBackend.repositories.TourPackageRepository;
import mingeso.first.travelAgencyBackend.repositories.UserRepository;

import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TourPackageRepository packageRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          TourPackageRepository packageRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
    }
    public BookingEntity getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
    }

    public List<BookingEntity> getBookingsByUserId(Long userId) {
        List<BookingEntity> bookings = bookingRepository.findByUserId(userId);
        if (bookings.isEmpty()) {
            return Collections.emptyList();
        }
        return bookings;
    }

    @Transactional
    public BookingEntity createBooking(BookingEntity booking) {

        TourPackageEntity tourPackage = packageRepository.findById(booking.getTourPackage().getId())
                .orElseThrow(() -> new BadRequestException("Package not found"));

        UserEntity user = userRepository.findById(booking.getUser().getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (booking.getPassengersCount() <= 0) {
            throw new BadRequestException("Passengers count must be greater than zero");
        }

        if (tourPackage.getAvailableSlots() < booking.getPassengersCount()) {
            throw new BadRequestException("Not enough available slots");
        }

        BigDecimal basePrice = tourPackage.getPrice();
        BigDecimal rawTotal = basePrice.multiply(BigDecimal.valueOf(booking.getPassengersCount()));

        BigDecimal discountPercentage = calculateAccumulatedDiscount(booking.getUser(), booking.getPassengersCount());

        BigDecimal totalDiscount = rawTotal.multiply(discountPercentage).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = rawTotal.subtract(totalDiscount).setScale(2, RoundingMode.HALF_UP);

        booking.setUser(user);
        booking.setTourPackage(tourPackage);
        booking.setBasePrice(basePrice);
        booking.setTotalAmount(finalAmount);
        booking.setTotalDiscount(totalDiscount);
        booking.setBookingDate(LocalDate.now());
        booking.setExpirationDate(LocalDate.now().plusDays(1));
        booking.setStateBooking(BookingStatus.PENDING_PAYMENT);

        tourPackage.setAvailableSlots(tourPackage.getAvailableSlots() - booking.getPassengersCount());
        packageRepository.save(tourPackage);

        return bookingRepository.save(booking);
    }

    private BigDecimal calculateAccumulatedDiscount(UserEntity user, int passengers) {
        BigDecimal discount = BigDecimal.ZERO;

        if (passengers >= 4) {
            discount = discount.add(new BigDecimal("0.10"));
        }

        long paidBookings = bookingRepository.countByUserAndStateBooking(user, BookingStatus.CONFIRMED);
        if (paidBookings >= 3) {
            discount = discount.add(new BigDecimal("0.15"));
        }

        if (discount.compareTo(new BigDecimal("0.20")) > 0) {
            discount = new BigDecimal("0.20");
        }

        return discount;
    }

    public List<BookingEntity> getUserHistoryByKeycloakId(String keycloakId) {
        UserEntity user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + keycloakId));
        return bookingRepository.findByUserId(user.getId());
    }

}
