package mingeso.first.travelAgencyBackend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import mingeso.first.travelAgencyBackend.enums.PackageStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tour_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourPackageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Integer duration;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer totalSlots;

    private Integer availableSlots;

    private String servicesIncluded;
    private String conditions;
    private String restrictions;
    private String tripType;
    private String season;
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageStatus status;
}