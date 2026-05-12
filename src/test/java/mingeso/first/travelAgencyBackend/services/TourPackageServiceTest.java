package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.enums.PackageStatus;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import mingeso.first.travelAgencyBackend.repositories.TourPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourPackageServiceTest {

    @Mock
    private TourPackageRepository packageRepository;

    @InjectMocks
    private TourPackageService packageService;

    private TourPackageEntity validPackage;

    @BeforeEach
    void setUp() {
        validPackage = new TourPackageEntity();
        validPackage.setId(1L);
        validPackage.setDestination("Easter Island");
        validPackage.setPrice(new BigDecimal("1500.00"));
        validPackage.setStartDate(LocalDate.now().plusDays(10));
        validPackage.setEndDate(LocalDate.now().plusDays(20));
        validPackage.setTotalSlots(10);
    }

    @Test
    void whenCreateValidPackage_thenSaveSuccessfully() {
        // Given
        when(packageRepository.save(any(TourPackageEntity.class))).thenReturn(validPackage);

        // When
        TourPackageEntity saved = packageService.createPackage(validPackage);

        // Then
        assertThat(saved.getStatus()).isEqualTo(PackageStatus.AVAILABLE);
        verify(packageRepository, times(1)).save(validPackage);
    }

    @Test
    void whenPriceIsZero_thenThrowBadRequestException() {
        // Given
        validPackage.setPrice(BigDecimal.ZERO);

        // When & Then
        assertThatThrownBy(() -> packageService.createPackage(validPackage))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("The package price must be greater than zero.");

        verify(packageRepository, never()).save(any());
    }

    @Test
    void whenEndDateIsBeforeStartDate_thenThrowBadRequestException() {
        // Given
        validPackage.setStartDate(LocalDate.now().plusDays(10));
        validPackage.setEndDate(LocalDate.now().plusDays(5));

        // When & Then
        assertThatThrownBy(() -> packageService.createPackage(validPackage))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("The end date must be after the start date.");
    }

    @Test
    void whenDeletePackage_thenStatusChangesToDeleted() {
        // Given
        when(packageRepository.findById(1L)).thenReturn(Optional.of(validPackage));
        when(packageRepository.save(any(TourPackageEntity.class))).thenReturn(validPackage);

        // When
        packageService.deletePackageLogical(1L);

        // Then
        assertThat(validPackage.getStatus()).isEqualTo(PackageStatus.DELETED);
        verify(packageRepository, times(1)).save(validPackage);

        verify(packageRepository, never()).delete(any());
    }
}