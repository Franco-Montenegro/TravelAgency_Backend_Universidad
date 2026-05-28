package mingeso.first.travelAgencyBackend.services;

import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.enums.BookingStatus;
import mingeso.first.travelAgencyBackend.enums.PackageStatus;
import mingeso.first.travelAgencyBackend.exceptions.BadRequestException;
import mingeso.first.travelAgencyBackend.repositories.BookingRepository;
import mingeso.first.travelAgencyBackend.repositories.TourPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourPackageServiceTest {

    @Mock
    private TourPackageRepository packageRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TourPackageService packageService;

    private TourPackageEntity validPackage;

    @BeforeEach
    void setUp() {
        validPackage = new TourPackageEntity();
        validPackage.setId(1L);
        validPackage.setName("Aventura Rapa Nui");
        validPackage.setDescription("Tour completo por la isla");
        validPackage.setDestination("Easter Island");
        validPackage.setPrice(new BigDecimal("1500.00"));
        validPackage.setStartDate(LocalDate.now().plusDays(10));
        validPackage.setEndDate(LocalDate.now().plusDays(20));
        validPackage.setTotalSlots(10);
        validPackage.setAvailableSlots(10);
        validPackage.setStatus(PackageStatus.AVAILABLE);
    }

    // ==========================================
    // TESTS: createPackage
    // ==========================================

    @Test
    @DisplayName("createPackage: Debe guardar exitosamente cuando el paquete es válido")
    void whenCreateValidPackage_thenSaveSuccessfully() {
        when(packageRepository.save(any(TourPackageEntity.class))).thenReturn(validPackage);

        TourPackageEntity saved = packageService.createPackage(validPackage);

        assertThat(saved.getStatus()).isEqualTo(PackageStatus.AVAILABLE);
        verify(packageRepository, times(1)).save(validPackage);
    }

    @Test
    @DisplayName("createPackage: Debe lanzar BadRequestException cuando el precio es cero o negativo")
    void whenPriceIsZero_thenThrowBadRequestException() {
        validPackage.setPrice(BigDecimal.ZERO);

        assertThatThrownBy(() -> packageService.createPackage(validPackage))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("The package price must be greater than zero.");

        verify(packageRepository, never()).save(any());
    }

    @Test
    @DisplayName("createPackage: Debe lanzar BadRequestException cuando la fecha de término es anterior a la de inicio")
    void whenEndDateIsBeforeStartDate_thenThrowBadRequestException() {
        validPackage.setStartDate(LocalDate.now().plusDays(10));
        validPackage.setEndDate(LocalDate.now().plusDays(5));

        assertThatThrownBy(() -> packageService.createPackage(validPackage))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("The end date must be after the start date.");
    }

    @Test
    @DisplayName("createPackage: Debe lanzar BadRequestException cuando las fechas de inicio y término son iguales")
    void whenEndDateIsEqualToStartDate_thenThrowBadRequestException() {
        validPackage.setStartDate(LocalDate.now().plusDays(10));
        validPackage.setEndDate(LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> packageService.createPackage(validPackage))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("The end date must be after the start date.");
    }

    @Test
    @DisplayName("createPackage: Debe lanzar BadRequestException si los cupos totales son menores o iguales a cero")
    void whenTotalSlotsIsZeroOrNegative_thenThrowBadRequestException() {
        validPackage.setTotalSlots(0);

        assertThatThrownBy(() -> packageService.createPackage(validPackage))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Total slots must be greater than zero.");
    }

    @Test
    @DisplayName("createPackage: Debe calcular la duración y asignar cupos correctamente")
    void whenCreatePackage_thenCalculateDurationAndSlotsCorrect() {
        validPackage.setStartDate(LocalDate.of(2026, 10, 1));
        validPackage.setEndDate(LocalDate.of(2026, 10, 10));
        validPackage.setTotalSlots(25);
        when(packageRepository.save(any(TourPackageEntity.class))).thenReturn(validPackage);

        TourPackageEntity saved = packageService.createPackage(validPackage);

        assertThat(saved.getDuration()).isEqualTo(9);
        assertThat(saved.getAvailableSlots()).isEqualTo(25);
    }

    // ==========================================
    // TESTS: deletePackageLogical
    // ==========================================

    @Test
    @DisplayName("deletePackageLogical: Debe cambiar el estado a DELETED de forma lógica")
    void whenDeletePackage_thenStatusChangesToDeleted() {
        when(packageRepository.findById(1L)).thenReturn(Optional.of(validPackage));
        when(packageRepository.save(any(TourPackageEntity.class))).thenReturn(validPackage);

        packageService.deletePackageLogical(1L);

        assertThat(validPackage.getStatus()).isEqualTo(PackageStatus.DELETED);
        verify(packageRepository, times(1)).save(validPackage);
        verify(packageRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deletePackageLogical: Debe lanzar BadRequestException si el paquete a eliminar no existe")
    void whenDeleteNonExistentPackage_thenThrowException() {
        when(packageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> packageService.deletePackageLogical(99L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Package not found");
    }

    // ==========================================
    // TESTS: getAllPackages & search & categories
    // ==========================================

    @Test
    @DisplayName("getAllPackages: Debe retornar la lista completa de paquetes")
    void getAllPackages_ShouldReturnList() {
        when(packageRepository.findAll()).thenReturn(List.of(validPackage));

        List<TourPackageEntity> result = packageService.getAllPackages();

        assertThat(result).hasSize(1);
        verify(packageRepository).findAll();
    }

    @Test
    @DisplayName("searchPackages: Debe invocar findPackagesByFilters con valores mapeados si los parámetros son nulos")
    void whenSearchPackagesWithNulls_thenMapToDefaultValues() {
        when(packageRepository.findPackagesByFilters(eq(""), eq(BigDecimal.ZERO), any(BigDecimal.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(validPackage));

        List<TourPackageEntity> result = packageService.searchPackages(null, null, null, null, null);

        assertThat(result).hasSize(1);
        verify(packageRepository).findPackagesByFilters(eq(""), eq(BigDecimal.ZERO), any(BigDecimal.class), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("getPackagesByCategory: Debe retornar solo los paquetes disponibles de esa categoría")
    void whenGetByCategory_thenReturnOnlyAvailable() {
        List<TourPackageEntity> packages = List.of(validPackage);
        when(packageRepository.findByCategoryAndStatus("ADVENTURE", PackageStatus.AVAILABLE))
                .thenReturn(packages);

        List<TourPackageEntity> result = packageService.getPackagesByCategory("ADVENTURE");

        assertThat(result).isNotEmpty();
        verify(packageRepository).findByCategoryAndStatus("ADVENTURE", PackageStatus.AVAILABLE);
    }

    // ==========================================
    // TESTS: updatePackageControlled (¡Los que faltaban!)
    // ==========================================

    @Test
    @DisplayName("updatePackageControlled: Debe actualizar con éxito si no existen reservas registradas")
    void updatePackageControlled_Success_NoBookings() {
        TourPackageEntity updatedData = new TourPackageEntity();
        updatedData.setName("Nuevo Nombre");
        updatedData.setDescription("Nueva Descripcion");
        updatedData.setDestination("San Pedro");
        updatedData.setPrice(new BigDecimal("1800.00"));
        updatedData.setTotalSlots(15);
        updatedData.setStartDate(LocalDate.now().plusDays(12));
        updatedData.setEndDate(LocalDate.now().plusDays(22));
        updatedData.setStatus(PackageStatus.AVAILABLE);

        when(packageRepository.findById(1L)).thenReturn(Optional.of(validPackage));
        when(bookingRepository.countByTourPackageIdAndStateBookingNot(1L, BookingStatus.CANCELLED)).thenReturn(0L);
        when(packageRepository.save(any(TourPackageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TourPackageEntity result = packageService.updatePackageControlled(1L, updatedData);

        assertThat(result.getName()).isEqualTo("Nuevo Nombre");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("1800.00"));
        assertThat(result.getTotalSlots()).isEqualTo(15);
        assertThat(result.getDestination()).isEqualTo("San Pedro");
    }

    @Test
    @DisplayName("updatePackageControlled: Debe lanzar RuntimeException si el paquete no existe")
    void updatePackageControlled_NotFound() {
        when(packageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> packageService.updatePackageControlled(99L, validPackage))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Package not found: ID: 99");
    }

    @Test
    @DisplayName("updatePackageControlled: Debe lanzar IllegalArgumentException si el precio es nulo o menor/igual a cero")
    void updatePackageControlled_InvalidPrice() {
        TourPackageEntity updatedData = new TourPackageEntity();
        updatedData.setPrice(BigDecimal.ZERO);

        when(packageRepository.findById(1L)).thenReturn(Optional.of(validPackage));

        assertThatThrownBy(() -> packageService.updatePackageControlled(1L, updatedData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price must be greater than zero");
    }

    @Test
    @DisplayName("updatePackageControlled: Debe lanzar IllegalArgumentException si los cupos son nulos o menores/iguales a cero")
    void updatePackageControlled_InvalidSlots() {
        TourPackageEntity updatedData = new TourPackageEntity();
        updatedData.setPrice(new BigDecimal("100"));
        updatedData.setTotalSlots(0);

        when(packageRepository.findById(1L)).thenReturn(Optional.of(validPackage));

        assertThatThrownBy(() -> packageService.updatePackageControlled(1L, updatedData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Slots must be greater than zero.");
    }

    @Test
    @DisplayName("updatePackageControlled: Debe lanzar IllegalStateException si se intentan modificar fechas teniendo reservas activas")
    void updatePackageControlled_WithBookings_TryToModifyDates_ThrowsException() {
        TourPackageEntity updatedData = new TourPackageEntity();
        updatedData.setPrice(new BigDecimal("1500.00"));
        updatedData.setTotalSlots(10);
        // Modificamos sutilmente las fechas
        updatedData.setStartDate(validPackage.getStartDate().plusDays(1));
        updatedData.setEndDate(validPackage.getEndDate());

        when(packageRepository.findById(1L)).thenReturn(Optional.of(validPackage));
        // Simulamos que hay 2 reservas activas
        when(bookingRepository.countByTourPackageIdAndStateBookingNot(1L, BookingStatus.CANCELLED)).thenReturn(2L);

        assertThatThrownBy(() -> packageService.updatePackageControlled(1L, updatedData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot modify dates with registered booking.");
    }

    @Test
    @DisplayName("updatePackageControlled: Debe lanzar IllegalStateException si el nuevo cupo total es menor a los pasajeros ya reservados")
    void updatePackageControlled_WithBookings_TryToLowerSlotsBelowReserved_ThrowsException() {
        TourPackageEntity updatedData = new TourPackageEntity();
        updatedData.setPrice(new BigDecimal("1500.00"));
        // Mantiene las fechas originales para pasar la primera validación
        updatedData.setStartDate(validPackage.getStartDate());
        updatedData.setEndDate(validPackage.getEndDate());
        // Intenta bajar el cupo total a 4
        updatedData.setTotalSlots(4);

        when(packageRepository.findById(1L)).thenReturn(Optional.of(validPackage));
        when(bookingRepository.countByTourPackageIdAndStateBookingNot(1L, BookingStatus.CANCELLED)).thenReturn(1L);
        // Simulamos que el total de pasajeros ya reservados en la BDD es de 6 personas
        when(bookingRepository.sumPassengersByTourPackageId(1L)).thenReturn(6L);

        assertThatThrownBy(() -> packageService.updatePackageControlled(1L, updatedData))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Total slots cannot be lower than reserved slots (6).");
    }
}