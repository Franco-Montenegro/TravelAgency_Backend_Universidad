package mingeso.first.travelAgencyBackend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import mingeso.first.travelAgencyBackend.enums.PackageStatus;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "tourPackages")
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

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate departureDate;

    @Column(nullable = false)
    private LocalDate returnDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private int availableStock;

    private String service;
    private String conditions;
    private String restriction;
    private String typeTrip;
    private String tempTrip;
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageStatus statePackage;
}
