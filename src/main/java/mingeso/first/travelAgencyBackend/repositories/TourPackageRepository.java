package mingeso.first.travelAgencyBackend.repositories;

import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.enums.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Repository
public interface TourPackageRepository extends JpaRepository<TourPackageEntity, Long> {

    List<TourPackageEntity> findByDestinationContainingIgnoreCaseAndStatus(String destination, PackageStatus status);

    @Query("SELECT p FROM TourPackageEntity p WHERE " +
            "LOWER(p.destination) LIKE LOWER(CONCAT('%', :destination, '%')) AND " +
            "p.price BETWEEN :minPrice AND :maxPrice AND " +
            "p.startDate >= :startDate AND " +
            "p.endDate <= :endDate AND " +
            "p.status = 'AVAILABLE'")
    List<TourPackageEntity> findPackagesByFilters(
            @Param("destination") String destination,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<TourPackageEntity> findByCategoryAndStatus(String category, PackageStatus status);
}
