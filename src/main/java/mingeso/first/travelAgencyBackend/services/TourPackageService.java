package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.enums.PackageStatus;
import mingeso.first.travelAgencyBackend.repositories.TourPackageRepository;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

        // Cupos totales mayores que cero
        if (tourPackage.getTotalSlots() <= 0) {
            throw new BadRequestException("Total slots must be greater than zero.");
        }

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
}