package mingeso.first.travelAgencyBackend.services;

import jakarta.transaction.Transactional;
import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.enums.BookingStatus;
import mingeso.first.travelAgencyBackend.enums.PackageStatus;
import mingeso.first.travelAgencyBackend.repositories.TourPackageRepository;
import mingeso.first.travelAgencyBackend.repositories.BookingRepository;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TourPackageService {

    private final TourPackageRepository packageRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public TourPackageService(TourPackageRepository packageRepository, BookingRepository bookingRepository) {
        this.packageRepository = packageRepository;
        this.bookingRepository = bookingRepository;
    }
    public TourPackageEntity createPackage(TourPackageEntity tourPackage) {
        if (tourPackage.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("The package price must be greater than zero.");
        }

        if (tourPackage.getEndDate().isBefore(tourPackage.getStartDate()) ||
                tourPackage.getEndDate().isEqual(tourPackage.getStartDate())) {
            throw new BadRequestException("The end date must be after the start date.");
        }
        int calculatedDuration = (int) java.time.temporal.ChronoUnit.DAYS.between(
                tourPackage.getStartDate(),
                tourPackage.getEndDate()
        );
        tourPackage.setDuration(calculatedDuration);

        if (tourPackage.getTotalSlots() <= 0) {
            throw new BadRequestException("Total slots must be greater than zero.");
        }
        tourPackage.setAvailableSlots(tourPackage.getTotalSlots());

        if (tourPackage.getStatus() == null) {
            tourPackage.setStatus(PackageStatus.AVAILABLE);
        }

        return packageRepository.save(tourPackage);
    }

    public void deletePackageLogical(Long id) {
        TourPackageEntity tourPackage = packageRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Package not found"));

        tourPackage.setStatus(PackageStatus.DELETED);
        packageRepository.save(tourPackage);
    }

    public List<TourPackageEntity> getAllPackages() {
        return packageRepository.findAll();
    }

    public List<TourPackageEntity> searchPackages(String destination, BigDecimal minPrice, BigDecimal maxPrice, LocalDate startDate, LocalDate endDate) {
        String dest = (destination == null) ? "" : destination;
        BigDecimal minP = (minPrice == null) ? BigDecimal.ZERO : minPrice;
        BigDecimal maxP = (maxPrice == null) ? new BigDecimal("999999999") : maxPrice;
        LocalDate startD = (startDate == null) ? LocalDate.now() : startDate;
        LocalDate endD = (endDate == null) ? LocalDate.now().plusYears(10) : endDate;

        return packageRepository.findPackagesByFilters(dest, minP, maxP, startD, endD);
    }

    public List<TourPackageEntity> getPackagesByCategory(String category) {
        return packageRepository.findByCategoryAndStatus(category, PackageStatus.AVAILABLE);
    }

    @Transactional
    public TourPackageEntity updatePackageControlled(Long id, TourPackageEntity updatedData) {
        TourPackageEntity existingPackage = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found: ID: " + id));

        if (updatedData.getPrice() == null || updatedData.getPrice().doubleValue() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (updatedData.getTotalSlots() == null || updatedData.getTotalSlots() <= 0) {
            throw new IllegalArgumentException("Slots must be greater than zero.");
        }

        long activeBookingsCount = bookingRepository.countByTourPackageIdAndStateBookingNot(id, BookingStatus.CANCELLED);

        if (activeBookingsCount > 0) {
            if (!existingPackage.getStartDate().equals(updatedData.getStartDate()) ||
                    !existingPackage.getEndDate().equals(updatedData.getEndDate())) {
                throw new IllegalStateException("Cannot modify dates with registered booking.");
            }

            long reservedSlots = bookingRepository.sumPassengersByTourPackageId(id);
            if (updatedData.getTotalSlots() < reservedSlots) {
                throw new IllegalStateException("Total slots cannot be lower than reserved slots (" + reservedSlots + ").");
            }
        }

        existingPackage.setName(updatedData.getName()); // 💡 ¡Fundamental!
        existingPackage.setDescription(updatedData.getDescription()); // 💡 ¡Fundamental!
        existingPackage.setDestination(updatedData.getDestination());
        existingPackage.setPrice(updatedData.getPrice());
        existingPackage.setTotalSlots(updatedData.getTotalSlots());
        existingPackage.setStartDate(updatedData.getStartDate());
        existingPackage.setEndDate(updatedData.getEndDate());
        existingPackage.setStatus(updatedData.getStatus());

        return packageRepository.save(existingPackage);
    }
}