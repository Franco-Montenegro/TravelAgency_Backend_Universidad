package mingeso.first.travelAgencyBackend.controllers;

import mingeso.first.travelAgencyBackend.entities.TourPackageEntity;
import mingeso.first.travelAgencyBackend.services.TourPackageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/packages")
@CrossOrigin("*")
public class TourPackageController {

    private final TourPackageService packageService;

    public TourPackageController(TourPackageService packageService) {
        this.packageService = packageService;
    }

    @PostMapping("/")
    public ResponseEntity<TourPackageEntity> createPackage(@RequestBody TourPackageEntity tourPackage) {
        TourPackageEntity newPackage = packageService.createPackage(tourPackage);
        return new ResponseEntity<>(newPackage, HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<List<TourPackageEntity>> getAllPackages() {
        List<TourPackageEntity> packages = packageService.getAllPackages();
        return ResponseEntity.ok(packages);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        packageService.deletePackageLogical(id);
        return ResponseEntity.noContent().build();
    }
}