package kigali.clinic.rw.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.service.SpecializationService;

@RestController
@RequestMapping("/api/specialization")
public class SpecializationController {

    private final SpecializationService specializationService;

    public SpecializationController(SpecializationService specializationService) {
        this.specializationService = specializationService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveSpecialization(@RequestBody Specialization specialization) {
        String message = specializationService.saveSpecialization(specialization);
        if ("Specialization is saved successfully".equals(message)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        }
        return ResponseEntity.status(errorStatus(message)).body(message);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        return ResponseEntity.ok(specializationService.getAllSpecializations());
    }

    @GetMapping("/unused")
    public ResponseEntity<List<Specialization>> getUnusedSpecializations() {
        return ResponseEntity.ok(specializationService.getUnusedSpecializations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSpecializationById(@PathVariable UUID id) {
        Optional<Specialization> specialization = specializationService.getSpecializationById(id);
        if (specialization.isPresent()) {
            return ResponseEntity.ok(specialization.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Specialization not found");
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<?> getDoctorsBySpecialization(@PathVariable UUID id) {
        Optional<List<Doctor>> doctors = specializationService.getDoctorsBySpecialization(id);
        if (doctors.isPresent()) {
            return ResponseEntity.ok(doctors.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Specialization not found");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateSpecialization(
            @PathVariable UUID id,
            @RequestBody Specialization specialization) {
        String message = specializationService.updateSpecialization(id, specialization);
        if ("Specialization is updated successfully".equals(message)) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.status(errorStatus(message)).body(message);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteSpecialization(@PathVariable UUID id) {
        String message = specializationService.deleteSpecialization(id);
        if ("Specialization is deleted successfully".equals(message)) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @PostMapping("/{id}/doctors/{doctorId}")
    public ResponseEntity<String> addDoctor(@PathVariable UUID id, @PathVariable UUID doctorId) {
        String message = specializationService.addDoctor(id, doctorId);
        if ("Doctor is added to specialization successfully".equals(message)) {
            return ResponseEntity.ok(message);
        }
        return ResponseEntity.status(errorStatus(message)).body(message);
    }

    @DeleteMapping("/{id}/doctors/{doctorId}")
    public ResponseEntity<String> removeDoctor(@PathVariable UUID id, @PathVariable UUID doctorId) {
        String message = specializationService.removeDoctor(id, doctorId);
        if ("Doctor is removed from specialization successfully".equals(message)) {
            return ResponseEntity.ok(message);
        }
        if (message.endsWith("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    private HttpStatus errorStatus(String message) {
        if (message.endsWith("not found")) {
            return HttpStatus.NOT_FOUND;
        }
        if (message.endsWith("is required")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.CONFLICT;
    }
}
