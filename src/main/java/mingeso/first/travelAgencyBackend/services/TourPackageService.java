package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.enums.PackageStatus;
import mingeso.first.travelAgencyBackend.repositories.TourPackageRepository;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TourPackageService {

    private final TourPackageRepository packageRepository;

    public TourPackageService(TourPackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    public TourPackageEntity createPackage(TourPackageEntity tourPackage) {
        // Precio mayor que cero
        if (tourPackage.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("The package price must be greater than zero.");
        }

        // Fecha de término posterior al inicio
        if (tourPackage.getEndDate().isBefore(tourPackage.getStartDate()) ||
                tourPackage.getEndDate().isEqual(tourPackage.getStartDate())) {
            throw new BadRequestException("The end date must be after the start date.");
        }
        int calculatedDuration = (int) java.time.temporal.ChronoUnit.DAYS.between(
                tourPackage.getStartDate(),
                tourPackage.getEndDate()
        );
        tourPackage.setDuration(calculatedDuration);

        // Cupos totales mayores que cero
        if (tourPackage.getTotalSlots() <= 0) {
            throw new BadRequestException("Total slots must be greater than zero.");
        }
        tourPackage.setAvailableSlots(tourPackage.getTotalSlots());

        // Seteo de estado inicial por defecto
        if (tourPackage.getStatus() == null) {
            tourPackage.setStatus(PackageStatus.AVAILABLE);
        }

        return packageRepository.save(tourPackage);
    }

    // Borrado Lógico
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
        // filtros vacíos -> establecer valores por defecto
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
}